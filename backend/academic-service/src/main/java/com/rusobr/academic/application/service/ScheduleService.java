package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.*;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.attendances.journal.AttendanceSimpleResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.homework.HomeworkSimpleResponse;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.DiaryDayDto;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.DiaryLessonDto;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.DiaryWeekResponse;
import com.rusobr.academic.web.dto.scheduleLesson.studentDiary.ScheduleLessonDiaryDto;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.academic.web.dto.scheduleLesson.*;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentRequest;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleLessonRepository scheduleLessonRepository;
    private final UserClient userClient;
    private final ScheduleLessonMapper scheduleLessonMapper;
    private final TeachingAssignmentService teachingAssignmentService;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final ScheduleGeneratorService scheduleGeneratorService;
    private final AttendanceMapper attendanceMapper;
    private final HomeworkMapper homeworkMapper;
    private final GradeMapper gradeMapper;

    @Lazy
    @Autowired
    private ScheduleService self;

    public List<ScheduleLessonResponse> getByDate(Long studentId, LocalDate date) {
        return scheduleLessonRepository.getScheduleByDate(studentId, date.getDayOfWeek(), date)
                .stream().map(scheduleLessonMapper::toScheduleLessonResponse).toList();
    }

    @Cacheable(value = "schedulesByStudentId", key = "#studentId + '#' + #startDate + '#' + #endDate")
    @Transactional(readOnly = true)
    public DiaryWeekResponse getByStudentId(Long studentId, LocalDate startDate, LocalDate endDate) {
        List<ScheduleLesson> scheduleLessons = scheduleLessonRepository
                .findDiaryScheduleByStudentId(studentId, startDate, endDate);
        List<Long> ids = scheduleLessons.stream().map(ScheduleLesson::getId).toList();

        List<LessonInstance> lessonInstances = lessonInstanceRepository
                .findLessonInstancesByScheduleId(ids, startDate, endDate);

        Map<Long, List<GradeResponse>> mappedGrades = lessonInstanceRepository
                .findLessonInstanceGradesByPeriodAndStudent(ids, startDate, endDate, studentId)
                .stream().collect(Collectors.groupingBy(
                        LessonInstance::getId,
                        LinkedHashMap::new,
                        Collectors.flatMapping(
                                li -> li.getGrades().stream().map(gradeMapper::toGradeResponseDto),
                                Collectors.toList()
                        )
                ));

        Map<Long, AttendanceSimpleResponse> mappedAttendances = lessonInstanceRepository
                .findLessonInstanceAttendancesByPeriodAndStudent(ids, startDate, endDate, studentId)
                .stream().collect(Collectors.toMap(
                        LessonInstance::getId,
                        li -> li.getAttendances().stream().findFirst()
                                .map(attendanceMapper::toAttendanceSimpleResponse).orElseThrow()
                ));

        Map<Long, List<HomeworkSimpleResponse>> mappedHomeworks = lessonInstanceRepository
                .findLessonInstanceHomeworksByPeriodAndStudent(ids, startDate, endDate)
                .stream().collect(Collectors.groupingBy(
                        LessonInstance::getId,
                        LinkedHashMap::new,
                        Collectors.flatMapping(
                                li -> li.getHomeworks().stream().map(homeworkMapper::toHomeworkSimpleResponse),
                                Collectors.toList()
                        )
                ));

        Map<Long, ScheduleLessonDiaryDto> mappedSchedule = scheduleLessons.stream()
                .map(scheduleLessonMapper::toScheduleLessonDiaryDto).collect(
                        Collectors.toMap(
                                ScheduleLessonDiaryDto::id,
                                s -> s
                        ));

        Map<LocalDate, List<DiaryLessonDto>> byDate = lessonInstances.stream()
                .collect(Collectors.groupingBy(
                        LessonInstance::getLessonDate,
                        LinkedHashMap::new,
                        Collectors.mapping(li -> {
                            ScheduleLessonDiaryDto currentSchedule = mappedSchedule.get(li.getScheduleLesson().getId());
                            return new DiaryLessonDto(
                                    currentSchedule.lessonNumber(), currentSchedule.subject(), currentSchedule.id(), li.getId(), currentSchedule.classRoom(),
                                    mappedGrades.getOrDefault(li.getId(), List.of()),
                                    mappedAttendances.getOrDefault(li.getId(), null),
                                    mappedHomeworks.getOrDefault(li.getId(), List.of())
                            );
                        }, Collectors.toList())
                ));

        List<DiaryDayDto> days = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new DiaryDayDto(
                        e.getKey().getDayOfWeek(),
                        e.getKey(),
                        e.getValue().stream().sorted(Comparator.comparing(DiaryLessonDto::lessonNumber))
                                .toList()
                ))
                .toList();

        return new DiaryWeekResponse(startDate, endDate, days);
    }

    @Cacheable(value = "schedulesByTeacherIdDate", key = "#teacherId + '#' + #date")
    public List<TeacherScheduleItem> getByTeacherIdDate(Long teacherId, LocalDate date) {
        List<LessonInstance> todaySchedules = scheduleLessonRepository.findTeacherScheduleByDate(teacherId, date);
        return todaySchedules.stream().map(scheduleLessonMapper::toTeacherScheduleItem).toList();
    }

    @Cacheable(value = "schedulesByTeacherIdPeriod", key = "#teacherId + '#' + #startDate + '#' + #endDate")
    public Map<DayOfWeek, List<TeacherScheduleItem>> getByTeacherIdPeriod(Long teacherId, LocalDate startDate, LocalDate endDate) {
        List<LessonInstance> todaySchedules = scheduleLessonRepository.findTeacherScheduleByPeriod(teacherId, startDate, endDate);
        List<TeacherScheduleItem> mappedSchedule = todaySchedules.stream().map(scheduleLessonMapper::toTeacherScheduleItem).toList();

        return mappedSchedule.stream().collect(
                Collectors.groupingBy(
                        TeacherScheduleItem::dayOfWeek,
                        LinkedHashMap::new,
                        Collectors.toList()
                )
        );
    }

    public Map<DayOfWeek, List<SchoolLessonResponse>> getWeekSchedule(Long studentId) {
        List<SchoolLessonResponse> sortedRes = scheduleLessonRepository.findAllByStudentId(studentId).stream()
                .map(scheduleLessonMapper::toSchoolLessonResponse)
                .sorted(Comparator.comparing(SchoolLessonResponse::dayOfWeek)
                        .thenComparing(SchoolLessonResponse::lessonNumber))
                .toList();

        return sortedRes.stream()
                .collect(
                        Collectors.groupingBy(
                                SchoolLessonResponse::dayOfWeek,
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                );
    }

    @Cacheable(value = "schedulesByClass", key = "#classId + '#' + #date")
    public Map<DayOfWeek, Map<Integer, List<ScheduleLessonDto>>> getByClass(Long classId, LocalDate date) {
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = date.with(DayOfWeek.SUNDAY);
        List<ScheduleLesson> scheduleLessons = scheduleLessonRepository.findClassSchedule(classId, monday, sunday);

        List<Long> teacherIds = scheduleLessons.stream().map(scheduleLesson ->
                scheduleLesson.getTeachingAssignment().getTeacherId()).distinct().toList();
        Map<Long, UserFeignResponse> teachers = userClient.getBatchTeachers(teacherIds).found()
                .stream().collect(Collectors.toMap(
                        UserFeignResponse::id,
                        userFeignResponse -> userFeignResponse
                ));

        return scheduleLessons.stream()
                .map((sl ->
                        scheduleLessonMapper.toDto(
                                sl,
                                teachers.get(sl.getTeachingAssignment().getTeacherId())
                        )
                )).collect(
                        Collectors.groupingBy(
                                ScheduleLessonDto::dayOfWeek,
                                () -> new EnumMap<>(DayOfWeek.class),
                                Collectors.groupingBy(
                                        ScheduleLessonDto::lessonNumber,
                                        TreeMap::new,
                                        Collectors.toList()
                                )
                        )
                );
    }

    public ScheduleLessonDetails getDetails(Long scheduleId) {
        ScheduleLesson scheduleLesson = self.getDetailsTransactional(scheduleId);
        UserFeignResponse teacher = userClient
                .getTeacherSimpleById(scheduleLesson.getTeachingAssignment().getTeacherId());
        return scheduleLessonMapper.toDetails(scheduleLesson, teacher);
    }

    @Transactional(readOnly = true)
    public ScheduleLesson getDetailsTransactional(Long scheduleId) {
        return scheduleLessonRepository.getDetails(scheduleId)
                .orElseThrow(() ->
                    new NotFoundException("Schedule with id: %d not found".formatted(scheduleId),
                            AcademicExceptionCode.SCHEDULE_NOT_FOUND)
                );
    }

    @CacheEvict(value = {"schedulesByStudentId", "schedulesByClass", "lessonInstancesByAssignment"}, allEntries = true)
    public void create(ScheduleLessonRequest scheduleLessonRequest) {
        userClient.getTeacherById(scheduleLessonRequest.teacherId());
        self.createTransactional(scheduleLessonRequest);
        log.info("Create schedule request={}", scheduleLessonRequest);
    }

    @Transactional
    public void createTransactional(ScheduleLessonRequest slReq) {
        TeachingAssignment teachingAssignment = teachingAssignmentService.createOrGet(new TeachingAssignmentRequest(
                slReq.classId(),
                slReq.subjectId(),
                slReq.teacherId(),
                slReq.classGroupId()));

        // Проверяем что у класса этот слот (день + номер урока) уже не занят
        // Или если есть подгруппа проверяем не занята ли она
        if (scheduleLessonRepository.existsActiveByClassSlot(
                slReq.classId(),
                slReq.dayOfWeek(),
                slReq.lessonNumber(),
                slReq.validFrom(),
                slReq.classGroupId()
        )) {
            throw new ConflictException("Slot is already taken for this class", AcademicExceptionCode.SCHEDULE_SLOT_ALREADY_TAKEN);
        }

        // Проверяем что учитель не ведёт другой урок в этот же слот
        if (scheduleLessonRepository.existsByTeacherSlot(
                slReq.teacherId(),
                slReq.dayOfWeek(),
                slReq.lessonNumber(),
                slReq.validFrom()
        )) {
            throw new ConflictException("Schedule lesson already exists", AcademicExceptionCode.SCHEDULE_ALREADY_EXIST);
        }

        ScheduleLesson scheduleLesson = scheduleLessonMapper.toEntity(slReq, teachingAssignment);
        scheduleLessonRepository.save(scheduleLesson);

        // Создаем lessonInstance наперед
        scheduleGeneratorService.generateInstanceForLesson(scheduleLesson);
    }

    @CacheEvict(value = {"schedulesByStudentId", "schedulesByClass", "lessonInstancesByAssignment"}, allEntries = true)
    @Transactional
    public void delete(Long scheduleId) {
        if (!scheduleLessonRepository.existsById(scheduleId)) {
            throw new NotFoundException("Schedule with id: %d not found".formatted(scheduleId),
                    AcademicExceptionCode.SCHEDULE_NOT_FOUND);
        }

        if (lessonInstanceRepository.existsAnyDataForSchedule(scheduleId)) {
            throw new ConflictException("Cannot delete schedule with id %s: there are already recorded data"
                    .formatted(scheduleId),
                    AcademicExceptionCode.SCHEDULE_HAS_DATA);
        }

        lessonInstanceRepository.deleteLessonInstancesByScheduleLessonId(scheduleId);
        scheduleLessonRepository.deleteById(scheduleId);
        log.info("Schedule deleted: id={}", scheduleId);
    }

    @CacheEvict(value = {"schedulesByStudentId", "lessonInstancesByAssignment"}, allEntries = true)
    @Transactional
    public void close(Long scheduleId, LocalDate closeDate) {
        ScheduleLesson scheduleLesson = scheduleLessonRepository.findWithTeachingAssignmentById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule with id: %d not found".formatted(scheduleId),
                        AcademicExceptionCode.SCHEDULE_NOT_FOUND));

        if (scheduleLesson.getValidTo() != null && !scheduleLesson.getValidTo().isAfter(LocalDate.now())) {
            throw new ConflictException("Schedule with id: %d is already closed".formatted(scheduleId),
                    AcademicExceptionCode.SCHEDULE_ALREADY_CLOSED);
        }

        if (lessonInstanceRepository.existsDataAfterDate(scheduleId, closeDate)) {
            throw new ConflictException("Cannot close schedule on %s: there are already recorded grades/attendance after this date"
                    .formatted(closeDate),
                    AcademicExceptionCode.SCHEDULE_HAS_DATA_AFTER_CLOSE_DATE);
        }

        scheduleLesson.setValidTo(closeDate);
        scheduleLessonRepository.save(scheduleLesson);

        // Удаляем lessonInstance к которым ничего не привязано
        lessonInstanceRepository.softDeleteFutureEmptyAfterDate(scheduleId, closeDate);
        log.info("Schedule closed: scheduleId={}, closeDate={}", scheduleId, closeDate);
    }

    @Transactional
    public void load(Long classId, LocalDate fromDate, LocalDate toDate) {
        List<ScheduleLesson> scheduleLessons = scheduleLessonRepository.findAllByClassIdAndPeriod(
                classId,
                fromDate,
                toDate
        );

        for (ScheduleLesson sl : scheduleLessons) {
            scheduleGeneratorService.generateInstanceBetween(sl, fromDate, toDate);
        }
        log.info("load schedule: classId={}, fromDate={}, toDate={}", classId, fromDate, toDate);
    }

}

package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.LessonInstanceMapper;
import com.rusobr.academic.infrastructure.mapper.ScheduleLessonMapper;
import com.rusobr.academic.infrastructure.persistence.repository.LessonInstanceRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ScheduleLessonRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.lessonInstance.DiaryLessonInstanceDto;
import com.rusobr.academic.web.dto.scheduleLesson.*;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final LessonInstanceService lessonInstanceService;
    private final LessonInstanceMapper lessonInstanceMapper;

    @Lazy
    @Autowired
    private ScheduleService self;

    public List<ScheduleLessonResponse> getScheduleByDate(Long studentId, DayOfWeek dayOfWeek, LocalDate date) {
        return scheduleLessonRepository.getScheduleByDate(studentId, dayOfWeek, date);
    }

    @Transactional(readOnly = true)
    public List<DiaryScheduleDto> getDiaryScheduleByStudentId(Long studentId, LocalDate startDate, LocalDate endDate) {
        //Получаем шаблон расписания по периоду и собираем их id в отдельный List
        List<ScheduleLesson> scheduleLessons = scheduleLessonRepository
                .findDiaryScheduleByStudentId(studentId, startDate, endDate);
        List<Long> ids = scheduleLessons.stream().map(ScheduleLesson::getId).toList();

        //Собираем детализацию (посещаемость, оценки, дз), маппим в dto
        List<LessonInstance> lessonInstances = lessonInstanceRepository
                .findDiaryAcademicPerformanceByStudentId(ids, startDate, endDate, studentId);
        List<DiaryLessonInstanceDto> diaryLessonInstances = lessonInstances.stream()
                .map(lessonInstance -> lessonInstanceMapper.toDiaryLessonInstance(lessonInstance)).toList();
        //Упаковываем в map, где key это scheduleId, а тело фильтруем отсекая лишние записи
        // в связи с join fetch собираются все записи класса, а не только ученика
        Map<Long, DiaryLessonInstanceDto> mappedDiaryLessonInstances = diaryLessonInstances.stream().collect(
            Collectors.toMap(
                    DiaryLessonInstanceDto::scheduleId,
                    li -> filterByStudent(li, studentId)
            )
        );

        //Отсекаем чужие записи и мапим, а затем сортируем список по дню недели
        return scheduleLessons.stream()
                .map(sl -> {
                    DiaryLessonInstanceDto instance = mappedDiaryLessonInstances.get(sl.getId());
                    if (instance != null && instance.grades().isEmpty() && instance.attendances().isEmpty()
                            && instance.homework() == null) {
                        instance = null;
                    }
                    return scheduleLessonMapper.toDiaryScheduleDto(sl, instance);
                })
                .sorted(Comparator.comparingInt(dto -> dto.dayOfWeek().getValue()))
                .toList();
    }

    private DiaryLessonInstanceDto filterByStudent(DiaryLessonInstanceDto dto, Long studentId) {
        return new DiaryLessonInstanceDto(
                dto.id(),
                dto.scheduleId(),
                dto.lessonDate(),
                dto.attendances().stream().filter(a -> Objects.equals(a.studentId(), studentId)).toList(),
                dto.grades().stream().filter(g -> Objects.equals(g.studentId(), studentId)).toList(),
                dto.homework()
        );
    }

    public Map<DayOfWeek, List<SchoolLessonResponse>> getWeekSchedule(Long studentId) {
        List<SchoolLessonResponse> sortedRes = scheduleLessonRepository.findAllByStudentId(studentId).stream()
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

    public Map<DayOfWeek, List<ScheduleLessonDto>> getClassSchedule(Long classId, LocalDate date) {
        List<ScheduleLesson> scheduleLessons = scheduleLessonRepository.findClassSchedule(classId, date);

        List<Long> teacherIds = scheduleLessons.stream().map(scheduleLesson ->
                scheduleLesson.getTeachingAssignment().getTeacherId()).distinct().toList();

        Map<Long, UserFeignResponse> teachers = userClient.getBatchTeachers(teacherIds)
                .stream().collect(Collectors.toMap(
                        UserFeignResponse::id,
                        userFeignResponse -> userFeignResponse
                ));

        return scheduleLessons.stream()
                .map((scheduleLesson ->
                        scheduleLessonMapper.toDto(
                                scheduleLesson,
                                teachers.get(scheduleLesson.getTeachingAssignment().getTeacherId())
                        )
                )).collect(
                        Collectors.groupingBy(
                                ScheduleLessonDto::dayOfWeek,
                                () -> new EnumMap<>(DayOfWeek.class),
                                Collectors.toList()
                        )
                );
    }

    public void create(ScheduleLessonRequest scheduleLessonRequest) {
        userClient.getTeacherById(scheduleLessonRequest.teacherId());
        self.createTransactional(scheduleLessonRequest);
    }

    @Transactional
    public void createTransactional(ScheduleLessonRequest scheduleLessonRequest) {
        // С запроса не приходит teaching_assignments, а приходит связка teacher_subjects и отдельно classId
        TeachingAssignment teachingAssignment = teachingAssignmentService.createOrGet(new TeachingAssignmentRequest(
                scheduleLessonRequest.classId(),
                scheduleLessonRequest.subjectId(),
                scheduleLessonRequest.teacherId()));

        // Проверяем что у класса этот слот (день + номер урока) уже не занят
        if (scheduleLessonRepository.existsActiveByClassSlot(
                scheduleLessonRequest.classId(),
                scheduleLessonRequest.dayOfWeek(),
                scheduleLessonRequest.lessonNumber(),
                scheduleLessonRequest.validFrom()
        )) {
            throw new ConflictException("Slot is already taken for this class");
        }

        // Проверяем что учитель не ведёт другой урок в этот же слот
        if (scheduleLessonRepository.existsActiveByTeachingAssignmentSlot(
            teachingAssignment.getId(),
            scheduleLessonRequest.dayOfWeek(),
            scheduleLessonRequest.lessonNumber(),
            scheduleLessonRequest.validFrom()
        )) {
            throw new ConflictException("Schedule lesson already exists");
        }

        ScheduleLesson scheduleLesson = scheduleLessonMapper.toEntity(scheduleLessonRequest, teachingAssignment);
        scheduleLessonRepository.save(scheduleLesson);

        // Создаем lessonInstance наперед
        lessonInstanceService.generateForLesson(scheduleLesson);
    }

    @Transactional
    public void close(Long scheduleId, LocalDate closeDate) {
        ScheduleLesson scheduleLesson = scheduleLessonRepository.findWithTeachingAssignmentById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule with id " + scheduleId + " not found"));

        if (scheduleLesson.getValidTo() != null && !scheduleLesson.getValidTo().isAfter(LocalDate.now())) {
            throw new ConflictException("Schedule with id " + scheduleId + " is already closed");
        }

        scheduleLesson.setValidTo(closeDate);
        scheduleLessonRepository.save(scheduleLesson);

        // Удаляем lessonInstance к которым ничего не привязано
        lessonInstanceRepository.softDeleteFutureEmptyAfterDate(scheduleId, closeDate);
    }

    @Transactional
    public void load(Long classId, LocalDate fromDate, LocalDate toDate) {
        List<ScheduleLesson> scheduleLessons = scheduleLessonRepository.findAllByClassIdAndPeriod(
                classId,
                fromDate,
                toDate
        );

        for (ScheduleLesson sl : scheduleLessons) {
            lessonInstanceService.generateBetween(sl, fromDate, toDate);
        }

    }

    //todo расписание для учителя какие уроки у нее

}

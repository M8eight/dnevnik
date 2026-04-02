package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.grade.GetGradeDataDto;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
import com.rusobr.academic.web.dto.userService.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final SchoolClassRepository schoolClassRepository;
    private final UserClient userClient;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final GradeMapper gradeMapper;

    private final GradeDataService gradeDataService;

    public List<UserResponse> getUsersIdFromClass(Long classId) {
        List<Long> userIds = schoolClassRepository.getStudentIdsFromSchoolClasses(classId);
        log.info("userIds: {}", userIds);
        return userClient.getBatchUsers(userIds);
    }

    public GradeJournalResponse getClassGrades(Long teachingAssignmentId, LocalDate date) {
        log.info("getClassGrades({})", teachingAssignmentId);

        if (date == null) {
            date = LocalDate.now();
        }

        //Получаем classId по teachingAssignmentId
        Long classId = teachingAssignmentRepository.findByIdWithClassId(teachingAssignmentId)
                .orElseThrow(() -> new NotFoundException("Not Found ClassId in findByIdWithClassId"));

        //Получаем список учеников feign, левая колонка (переиспользуем метод getUsersIdFromClass)
        List<UserResponse> classStudents = getUsersIdFromClass(classId);
        log.info("classStudents: {}", classStudents);

        GetGradeDataDto journalData = gradeDataService.getGradeData(teachingAssignmentId, date);

        return gradeMapper.toGradeJournalResponse(classStudents, journalData);
    }
}

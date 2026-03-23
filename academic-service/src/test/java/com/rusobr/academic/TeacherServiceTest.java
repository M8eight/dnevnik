package com.rusobr.academic;

import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.infrastructure.service.GradeDataService;
import com.rusobr.academic.infrastructure.service.TeacherService;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodDto;
import com.rusobr.academic.web.dto.grade.GradeJournalData;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
import com.rusobr.academic.web.dto.grade.TeacherGradeDto;
import com.rusobr.academic.web.dto.userService.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {

    @InjectMocks
    private TeacherService teacherService;

    @Mock
    private SchoolClassRepository schoolClassRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private TeachingAssignmentRepository teachingAssignmentRepository;
    @Mock
    private GradeMapper gradeMapper;
    @Mock
    private GradeDataService gradeDataService;

    @Test
    @DisplayName("Вернуть пользователей по classId")
    void shouldGetUsersResponse() {
        Long classId = 1L;
        List<Long> userIds = List.of(1L, 2L);
        List<UserResponse> usersRes = List.of(
                new UserResponse("Алексей", "Кочетыгов", "abc-123", 1L),
                new UserResponse("Алексей", "Кочетыгов", "abc-123", 2L)
        );

        when(schoolClassRepository.getStudentIdsFromSchoolClasses(classId)).thenReturn(userIds);
        when(userClient.getBatchUsers(userIds)).thenReturn(usersRes);

        List<UserResponse> res = teacherService.getUsersIdFromClass(classId);

        verify(schoolClassRepository).getStudentIdsFromSchoolClasses(classId);
        verify(userClient).getBatchUsers(userIds);
    }

    @Test
    @DisplayName("Вернуть полное расписание")
    void shouldGetClassGrades() {
        Long teachingAssignmentId = 1L;
        LocalDate date = LocalDate.now();
        List<UserResponse> usersRes = List.of(
                new UserResponse("Алексей", "Кочетыгов", "abc-123", 1L),
                new UserResponse("Алексей", "Кочетыгов", "abc-123", 2L)
        );
        GradeJournalData journalData = new GradeJournalData(
                List.of(LocalDate.of(2025, 9, 1)),
                List.of(new TeacherGradeDto(1L, 1L, 5, "TEST", LocalDate.of(2025, 9, 2))),
                new AcademicPeriodDto(
                        1L,
                        "Первая четверть",
                        "2025",
                        false,
                        LocalDate.of(2025, 9, 1),
                        LocalDate.of(2025, 10, 26)
                )
        );
        GradeJournalResponse resultDto = new GradeJournalResponse(usersRes, journalData.dates(), journalData.grades(), journalData.period());

        when(schoolClassRepository.getStudentIdsFromSchoolClasses(any())).thenReturn(List.of(1L));
        when(userClient.getBatchUsers(any())).thenReturn(usersRes);

        when(teachingAssignmentRepository.findByIdWithClassId(any())).thenReturn(Optional.of(1L));
        when(gradeDataService.getGradeData(any(), any())).thenReturn(journalData);
        when(gradeMapper.toGradeJournalResponse(any(), any())).thenReturn(resultDto);

        GradeJournalResponse res = teacherService.getClassGrades(teachingAssignmentId, date);

        verify(teachingAssignmentRepository).findByIdWithClassId(any());
        verify(gradeDataService).getGradeData(any(), any());


    }

}

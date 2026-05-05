package com.rusobr.academic.service;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.GradeMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.infrastructure.service.GradeDataService;
import com.rusobr.academic.infrastructure.service.TeacherService;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.grade.DateScheduleAssignDto;
import com.rusobr.academic.web.dto.grade.GetGradeDataDto;
import com.rusobr.academic.web.dto.grade.GradeJournalItemDto;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        assertEquals(2, res.size());
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

        // dates теперь List<DateScheduleAssignDto>
        List<DateScheduleAssignDto> dates = List.of(
                new DateScheduleAssignDto(LocalDate.of(2025, 9, 1), 10L)
        );

        GetGradeDataDto journalData = new GetGradeDataDto(
                dates,
                List.of(new GradeJournalItemDto(1L, 1L, 5, GradeType.TEST, LocalDate.of(2025, 9, 2))),
                new AcademicPeriodResponse(
                        1L,
                        "Первая четверть",
                        "2025",
                        false,
                        LocalDate.of(2025, 9, 1),
                        LocalDate.of(2025, 10, 26)
                )
        );

        GradeJournalResponse resultDto = new GradeJournalResponse(
                usersRes, journalData.dates(), journalData.grades(), journalData.period()
        );

        when(teachingAssignmentRepository.findByIdWithClassId(teachingAssignmentId))
                .thenReturn(Optional.of(1L));
        when(schoolClassRepository.getStudentIdsFromSchoolClasses(1L)).thenReturn(List.of(1L, 2L));
        when(userClient.getBatchUsers(any())).thenReturn(usersRes);
        when(gradeDataService.getGradeData(teachingAssignmentId, date)).thenReturn(journalData);
        when(gradeMapper.toGradeJournalResponse(usersRes, journalData)).thenReturn(resultDto);

        GradeJournalResponse res = teacherService.getClassGrades(teachingAssignmentId, date);

        verify(teachingAssignmentRepository).findByIdWithClassId(teachingAssignmentId);
        verify(gradeDataService).getGradeData(teachingAssignmentId, date);
        // Добавляем проверку маппера — он тоже должен вызываться
        verify(gradeMapper).toGradeJournalResponse(usersRes, journalData);

        assertNotNull(res);
        assertEquals(2, res.users().size());
    }
}

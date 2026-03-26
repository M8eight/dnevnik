package com.rusobr.academic.web.dto.grade;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodDto;
import com.rusobr.academic.web.dto.userService.UserResponse;

import java.time.LocalDate;
import java.util.List;

public record GradeJournalResponse(
        List<UserResponse> users,
        List<DateScheduleAssignDto> dates,
        List<TeacherGradeDto> grades,
        AcademicPeriodDto period
) {
}

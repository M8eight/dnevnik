package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.userService.UserResponse;

import java.util.List;

public record GradeJournalResponse(
        List<UserResponse> users,
        List<DateScheduleAssignDto> dates,
        List<GradeJournalItemDto> grades,
        AcademicPeriodResponse period
) {
}

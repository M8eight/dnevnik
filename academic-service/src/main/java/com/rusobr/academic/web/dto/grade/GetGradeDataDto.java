package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;

import java.util.List;

public record GetGradeDataDto(
   List<DateScheduleAssignDto> dates,
   List<GradeJournalItemDto> grades,
   AcademicPeriodResponse period
) {}

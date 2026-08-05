package com.rusobr.academic.web.dto.pdf;

import com.rusobr.academic.web.dto.grade.PeriodFinalGradeResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.common.dto.UserFeignResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record StudentPeriodFinalGradeReportDto(
        String title,
        String academicYearName,
        UserFeignResponse student,
        SchoolClassResponse schoolClass,
        List<PeriodFinalGradeResponse> periodFinalGrades
) {
}

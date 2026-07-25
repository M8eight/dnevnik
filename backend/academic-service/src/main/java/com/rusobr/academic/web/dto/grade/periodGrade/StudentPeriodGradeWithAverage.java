package com.rusobr.academic.web.dto.grade.periodGrade;

import com.rusobr.common.dto.UserFeignResponse;

import java.util.List;

public record StudentPeriodGradeWithAverage(
        UserFeignResponse user,
        List<PeriodGradeResponse> periodGrades,
        Double currentAverage
) {
}

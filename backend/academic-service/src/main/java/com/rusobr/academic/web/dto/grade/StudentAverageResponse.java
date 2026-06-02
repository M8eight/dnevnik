package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;

public record StudentAverageResponse(
        UserFeignResponse user,
        PeriodGradeResponse periodGrade,
        Double average
) {
}

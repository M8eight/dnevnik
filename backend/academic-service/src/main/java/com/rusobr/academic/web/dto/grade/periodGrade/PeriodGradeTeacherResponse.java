package com.rusobr.academic.web.dto.grade.periodGrade;

import com.rusobr.academic.web.dto.feign.UserFeignResponse;

import java.util.List;

public record PeriodGradeTeacherResponse(
        UserFeignResponse user,
        List<PeriodGradeResponse> periodGrades,
        Double currentAverage
) {
}

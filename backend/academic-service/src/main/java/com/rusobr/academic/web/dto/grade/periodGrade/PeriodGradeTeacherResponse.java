package com.rusobr.academic.web.dto.grade.periodGrade;

import com.rusobr.common.dto.UserFeignResponse;

import java.util.List;

public record PeriodGradeTeacherResponse(
        UserFeignResponse user,
        List<PeriodGradeResponse> periodGrades,
        Double currentAverage
) {
}

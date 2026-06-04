package com.rusobr.academic.web.dto.grade.finalGrade;

import com.rusobr.academic.web.dto.feign.UserFeignResponse;

import java.util.List;

public record FinalGradeTeacherResponse(
        UserFeignResponse user,
        List<FinalGradeResponse> finalGrades
) {
}

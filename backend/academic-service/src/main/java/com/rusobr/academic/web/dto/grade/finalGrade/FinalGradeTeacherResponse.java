package com.rusobr.academic.web.dto.grade.finalGrade;

import java.util.List;

public record FinalGradeTeacherResponse(
        List<StudentFinalGradesResponse> studentFinalGradesResponse,
        boolean isDegradedUsers
) {
}

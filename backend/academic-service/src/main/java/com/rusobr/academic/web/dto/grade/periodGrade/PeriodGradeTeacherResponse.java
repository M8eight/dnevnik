package com.rusobr.academic.web.dto.grade.periodGrade;

import java.util.List;

public record PeriodGradeTeacherResponse(
        List<StudentPeriodGradeWithAverage> studentPeriodGrades,
        boolean isDegradedUsers
) {
}

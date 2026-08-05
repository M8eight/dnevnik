package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;

import java.util.List;

public record PeriodFinalGradeResponse(
        String subjectName,
        List<PeriodGradeStudentResponse> periodGrades,
        FinalGradeResponse finalGrade
) {
}

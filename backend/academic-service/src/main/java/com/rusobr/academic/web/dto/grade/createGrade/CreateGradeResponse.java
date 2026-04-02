package com.rusobr.academic.web.dto.grade.createGrade;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public record CreateGradeResponse(
        Long studentId,
        int value,
        GradeType gradeType,
        Long gradeId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date
) {
}

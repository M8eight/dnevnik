package com.rusobr.academic.web.dto.lessonInstance;

import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public record GradeJournalProjection(
        String subjectName,
        Long gradeId,
        Integer value,
        Integer weight,
        GradeType gradeType,
        LocalDate date
) {
}

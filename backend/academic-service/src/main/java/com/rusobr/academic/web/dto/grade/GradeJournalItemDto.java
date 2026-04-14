package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public record GradeJournalItemDto(
        Long studentId,
        Long gradeId,
        Integer value,
        GradeType type,
        LocalDate date
) {}

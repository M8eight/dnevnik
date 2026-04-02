package com.rusobr.academic.web.dto.grade;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rusobr.academic.domain.enums.GradeType;

import java.time.LocalDate;

public record GradeJournalItemDto(
        Long studentId,
        Long gradeId,
        Integer value,
        GradeType type,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date
) {}

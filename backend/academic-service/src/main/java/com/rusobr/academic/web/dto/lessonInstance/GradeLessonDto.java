package com.rusobr.academic.web.dto.lessonInstance;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.web.dto.grade.WeightedGrade;

import java.time.LocalDate;

public record GradeLessonDto (
        Long gradeId,
        Integer value,
        Integer weight,
        GradeType gradeType,
        LocalDate date
) implements WeightedGrade{}

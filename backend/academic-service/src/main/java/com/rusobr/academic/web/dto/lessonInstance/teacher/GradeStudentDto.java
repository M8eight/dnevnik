package com.rusobr.academic.web.dto.lessonInstance.teacher;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.web.dto.grade.WeightedGrade;

public record GradeStudentDto (
        Long gradeId,
        Integer value,
        Integer weight,
        GradeType gradeType,
        Long studentId,
        Long lessonInstanceId
) implements WeightedGrade {}

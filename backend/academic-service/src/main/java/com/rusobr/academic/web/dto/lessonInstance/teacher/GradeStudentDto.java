package com.rusobr.academic.web.dto.lessonInstance.teacher;

import com.rusobr.academic.domain.enums.GradeType;

public record GradeStudentDto(
        Long gradeId,
        Integer value,
        Integer weight,
        GradeType gradeType,
        Long studentId,
        Long lessonInstanceId
) {}

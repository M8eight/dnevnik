package com.rusobr.academic.web.dto.grade.createGrade;

import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;

public record CreateGradeResponse(
        Long gradeId,
        Long studentId,
        LessonInstanceDto lessonInstance,
        Integer value,
        Integer weight,
        GradeType gradeType
) {
}

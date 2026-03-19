package com.rusobr.academic.web.dto.grade;

import java.time.LocalDate;

public record TeacherGradeDto(
        Long studentId,
        Long gradeId,
        Integer value,
        String type,
        LocalDate date
) {}

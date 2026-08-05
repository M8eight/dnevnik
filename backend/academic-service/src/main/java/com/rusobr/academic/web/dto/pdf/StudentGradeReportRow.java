package com.rusobr.academic.web.dto.pdf;

import com.rusobr.academic.web.dto.lessonInstance.GradeLessonDto;

import java.util.List;

public record StudentGradeReportRow(
        String subject,
        List<GradeLessonDto> grades,
        Double average
) {}
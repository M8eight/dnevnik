package com.rusobr.academic.web.dto.lessonInstance;

import java.util.List;

public record DatesGradesDto(
        String subject,
        List<GradeLessonDto> grades
) {
}

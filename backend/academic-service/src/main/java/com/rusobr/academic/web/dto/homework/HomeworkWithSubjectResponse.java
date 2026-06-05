package com.rusobr.academic.web.dto.homework;

public record HomeworkWithSubjectResponse(
        Long id,
        String text,
        String subjectName
) {
}

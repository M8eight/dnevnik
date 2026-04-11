package com.rusobr.academic.web.dto.homework;

public record HomeworkResponse(
        Long id,
        String text,
        String subjectName
) {
}

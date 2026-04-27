package com.rusobr.academic.web.dto.homework;

public record HomeworkRequest(
        String text,
        Long lessonInstanceId
) {}

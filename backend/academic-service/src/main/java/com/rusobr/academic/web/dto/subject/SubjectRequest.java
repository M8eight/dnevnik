package com.rusobr.academic.web.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubjectRequest(
        @NotBlank(message = "Название предмета не может быть пустым")
        @Size(max = 50, message = "Название предмета не может быть больше 50 символов")
        @Size(min = 3, message = "Название предмета не может быть меньше 3 символов")
        String subjectName
) {}
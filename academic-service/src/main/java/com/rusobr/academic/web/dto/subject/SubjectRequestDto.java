package com.rusobr.academic.web.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubjectRequestDto(

        @NotBlank(message = "Subject name must not be blank")
        @Size(max = 100, message = "Subject name must be less than 100 characters")
        String subjectName

) {}
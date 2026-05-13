package com.rusobr.user.web.dto.user.update;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserUpdateData(
        @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
        @Pattern(regexp = "^[a-zA-Z0-9._-]*$", message = "Логин содержит недопустимые символы")
        String username,
        @Size(max = 255, message = "Имя слишком длинное")
        @Pattern(
                regexp = "^[^()<>\\\\/\"']*$",
                message = "Имя содержит недопустимые символы (скобки, кавычки или слэши)"
        )
        String firstName,
        @Size(max = 255, message = "Фамилия слишком длинная")
        @Pattern(
                regexp = "^[^()<>\\\\/\"']*$",
                message = "Фамилия содержит недопустимые символы (скобки, кавычки или слэши)"
        )
        String lastName
) {
}

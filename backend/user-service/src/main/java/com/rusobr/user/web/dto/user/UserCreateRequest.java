package com.rusobr.user.web.dto.user;

import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.service.user.UserProfileDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest<T extends UserProfileDetails>(
        @Valid
        @NotNull(message = "Данные пользователя не могут быть пустыми")
        UserDataDto user,
        @NotEmpty(message = "Должна быть выбрана хотя бы одна роль")
        UserRole role,
        @NotNull(message = "Details не может быть null")
        T details
) {}

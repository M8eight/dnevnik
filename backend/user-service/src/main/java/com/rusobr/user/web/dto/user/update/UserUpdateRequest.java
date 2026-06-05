package com.rusobr.user.web.dto.user.update;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.web.dto.user.UserProfileDetails;
import com.rusobr.user.web.deserializer.UserProfileDetailsDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.Set;

public record UserUpdateRequest(
        Long userId,
        @Valid
        @NotNull(message = "Данные пользователя не могут быть пустыми")
        UserUpdateData user,
        @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
        String password,
        @NotEmpty(message = "Должна быть выбрана хотя бы одна роль")
        Set<UserRole> roles,
        @NotNull(message = "Details не может быть null")
        @JsonDeserialize(using = UserProfileDetailsDeserializer.class)
        Map<UserRole, UserProfileDetails> details
) {}

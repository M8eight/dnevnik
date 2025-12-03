package com.rusobr.user.web.dto;

import com.rusobr.user.domain.model.User;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link User}
 */
@Value
public class RequestUserDto implements Serializable {
    String username;
    String role;
}
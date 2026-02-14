package com.rusobr.user.web.dto;

import com.rusobr.user.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link User}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDtoRequest implements Serializable {
    String firstName;
    String lastName;
    String role;
    String username;
    String password;
}
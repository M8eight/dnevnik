package com.rusobr.user.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDtoResponse {
    private String firstName;
    private String keycloack_id;
    private String lastName;
}

package com.rusobr.user.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignRoleToUserRequestDto {
    private String userId;
    private String roleName;
    private String roleId;
}

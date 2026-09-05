package com.rusobr.academic.config.security;

import com.rusobr.academic.infrastructure.client.UserClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ParentSecurity {

    private final UserClient userClient;

    public ParentSecurity(@Lazy UserClient userClient) {
        this.userClient = userClient;
    }

    public boolean isChild(Long parentId, Long studentId) {
        return userClient.isChild(parentId, studentId);
    }

}

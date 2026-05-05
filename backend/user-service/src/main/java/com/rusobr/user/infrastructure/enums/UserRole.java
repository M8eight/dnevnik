package com.rusobr.user.infrastructure.enums;

public enum UserRole {
    ADMIN,
    STUDENT,
    PARENT,
    TEACHER;

    public String toSpringRole() {
        return "ROLE_" + this.name();
    }
}

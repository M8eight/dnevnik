package com.rusobr.common.enums;

public enum UserRole {
    ADMIN,
    STUDENT,
    PARENT,
    TEACHER;

    public String toSpringRole() {
        return "ROLE_" + this.name();
    }
}

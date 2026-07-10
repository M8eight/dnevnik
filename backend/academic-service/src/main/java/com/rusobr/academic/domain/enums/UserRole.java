package com.rusobr.academic.domain.enums;

public enum UserRole {
    ADMIN,
    STUDENT,
    PARENT,
    TEACHER;

    public String toSpringRole() {
        return "ROLE_" + this.name();
    }
}

package com.rusobr.user.jpaIT;

import com.rusobr.common.enums.UserRole;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;

import java.util.ArrayList;
import java.util.Set;

public final class TestData {

    private TestData() {
    }

    public static User user(String username, String firstName, String lastName) {
        return User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static User user(String username, String firstName, String lastName, Set<UserRole> roles) {
        return User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .build();
    }

    public static User user(String username, String firstName, String lastName, String keycloakId) {
        return User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .keycloakId(keycloakId)
                .build();
    }

    public static Student student(User user, String studyProfile) {
        return Student.builder()
                .user(user)
                .studyProfile(studyProfile)
                .build();
    }

    public static Student student(User user, String studyProfile, Parent parent) {
        return Student.builder()
                .user(user)
                .studyProfile(studyProfile)
                .parent(parent)
                .build();
    }

    public static Teacher teacher(User user, String email, String phoneNumber) {
        return Teacher.builder()
                .user(user)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
    }

    public static Parent parent(User user) {
        return Parent.builder()
                .user(user)
                .children(new ArrayList<>())
                .build();
    }

}

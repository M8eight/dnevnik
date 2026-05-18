package com.rusobr.academic.web.dto.feign;

public record TeacherDetails(
        String email,
        String phoneNumber
) implements UserProfileDetails {
}

package com.rusobr.user.web.dto.teacher;

import com.rusobr.user.web.dto.user.UserProfileDetails;

public record TeacherDetails(
    String email,
    String phoneNumber
) implements UserProfileDetails {
}

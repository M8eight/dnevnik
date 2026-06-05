package com.rusobr.user.web.dto.student;

import com.rusobr.user.web.dto.user.UserProfileDetails;

public record StudentDetails(
        String studyProfile
) implements UserProfileDetails {}

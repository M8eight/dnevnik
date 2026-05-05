package com.rusobr.user.web.dto.student;

import com.rusobr.user.infrastructure.service.user.UserProfileDetails;

public record StudentDetails(
        String studyProfile
) implements UserProfileDetails {}

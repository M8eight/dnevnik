package com.rusobr.academic.web.dto.feign;

public record TeacherResponse (
        UserResponse user,
        TeacherDetails details
) {}

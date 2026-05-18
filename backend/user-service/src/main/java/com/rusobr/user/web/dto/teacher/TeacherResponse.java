package com.rusobr.user.web.dto.teacher;

import com.rusobr.user.web.dto.user.UserResponse;

public record TeacherResponse(
        UserResponse user,
        TeacherDetails details
) {
}

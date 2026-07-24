package com.rusobr.academic.web.dto.feign;

import com.rusobr.common.dto.UserFeignResponse;

public record TeacherResponse (
        UserFeignResponse user,
        TeacherDetails details
) {}

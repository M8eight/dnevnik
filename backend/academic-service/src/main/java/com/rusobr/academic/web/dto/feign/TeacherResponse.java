package com.rusobr.academic.web.dto.feign;

public record TeacherResponse (
        UserFeignResponse user,
        TeacherDetails details
) {}

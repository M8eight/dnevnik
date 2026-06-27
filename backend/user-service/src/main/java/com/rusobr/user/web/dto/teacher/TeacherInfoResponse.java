package com.rusobr.user.web.dto.teacher;

import com.rusobr.user.web.dto.feign.TeacherAcademicFeignDto;
import lombok.Builder;

@Builder
public record TeacherInfoResponse(
        String phoneNumber,
        String email,
        TeacherAcademicFeignDto schoolDetails
) {
}

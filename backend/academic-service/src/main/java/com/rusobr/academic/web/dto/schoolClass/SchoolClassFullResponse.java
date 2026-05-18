package com.rusobr.academic.web.dto.schoolClass;

import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.feign.UserResponse;

import java.util.List;

public record SchoolClassFullResponse(
        Long id,
        String name,
        String year,
        TeacherResponse teacher,
        List<UserResponse> students
) {
}

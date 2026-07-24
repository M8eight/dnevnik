package com.rusobr.academic.web.dto.schoolClass;

import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.TeacherResponse;

public record SchoolClassFullResponse(
        Long id,
        String name,
        TeacherResponse teacher,
        Long classTeacherId,
        BatchUserResponse students
) {
}

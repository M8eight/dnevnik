package com.rusobr.user.web.dto.feign.teacherInfo;

import com.rusobr.user.web.dto.feign.SchoolClassResponse;

public record TeachingAssignmentResponse(
        Long id,
        SubjectResponse subject,
        SchoolClassResponse schoolClass
) {
}

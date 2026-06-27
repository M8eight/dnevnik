package com.rusobr.academic.web.dto.feign.teacherInfo;

import com.rusobr.academic.web.dto.subject.SubjectResponseDto;

public record TeacherSubjectRawResponse(
        SubjectResponseDto subject
) {
}

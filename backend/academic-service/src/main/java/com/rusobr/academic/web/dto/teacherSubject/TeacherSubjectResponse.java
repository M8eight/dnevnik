package com.rusobr.academic.web.dto.teacherSubject;

import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;

public record TeacherSubjectResponse(
        UserFeignResponse teacher,
        SubjectResponseDto subject
) {
}

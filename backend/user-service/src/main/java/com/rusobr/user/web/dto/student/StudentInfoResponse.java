package com.rusobr.user.web.dto.student;

import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.dto.user.UserResponse;

public record StudentInfoResponse(
    String studyProfile,
    UserResponse parent,
    SchoolClassResponse schoolClass,
    TeacherResponse classTeacher
) {
}

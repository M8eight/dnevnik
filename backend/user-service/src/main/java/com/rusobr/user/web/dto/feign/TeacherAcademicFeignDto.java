package com.rusobr.user.web.dto.feign;

import com.rusobr.user.web.dto.feign.teacherInfo.TeacherSubjectResponse;
import com.rusobr.user.web.dto.feign.teacherInfo.TeachingAssignmentResponse;

import java.util.List;

public record TeacherAcademicFeignDto(
        List<TeacherSubjectResponse> subjects,
        List<SchoolClassResponse> classes,
        List<TeachingAssignmentResponse> assignments
) {
}

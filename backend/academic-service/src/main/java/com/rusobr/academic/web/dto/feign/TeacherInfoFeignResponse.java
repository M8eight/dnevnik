package com.rusobr.academic.web.dto.feign;

import com.rusobr.academic.web.dto.feign.teacherInfo.TeacherSubjectRawResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record TeacherInfoFeignResponse(
        List<TeacherSubjectRawResponse> subjects,
        List<SchoolClassResponse> classes,
        List<TeachingAssignmentResponse> assignments
) {
}

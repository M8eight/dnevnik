package com.rusobr.user.web.dto.student;

import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.teacher.TeacherResponse;

public record StudentResponseDetail(
        Long id,
        Long userId,
        String keycloakId,
        String firstName,
        String lastName,
        String studyProfile,
        SchoolClassResponse schoolClass,
        TeacherResponse schoolClassTeacher
) {}

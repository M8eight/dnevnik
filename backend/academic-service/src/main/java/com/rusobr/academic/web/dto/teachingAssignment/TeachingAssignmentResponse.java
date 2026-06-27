package com.rusobr.academic.web.dto.teachingAssignment;

import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;

public record TeachingAssignmentResponse(
        Long id,
        SubjectResponseDto subject,
        SchoolClassResponse schoolClass
) {}

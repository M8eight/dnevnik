package com.rusobr.academic.web.dto.teachingAssignment;

public record TeachingAssignmentDetailsDto(
        Long teachingAssignmentId,
        Long schoolClassId,
        String schoolClassName,
        Long subjectId,
        String subjectName
) {
}

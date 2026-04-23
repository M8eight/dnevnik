package com.rusobr.academic.web.dto.teachingAssignment;

public record TeachingAssignmentWithSubjectProjection(
        Long teachingAssignmentId,
        Long schoolClassId,
        String schoolClassName,
        Long subjectId,
        String subjectName
) {
}

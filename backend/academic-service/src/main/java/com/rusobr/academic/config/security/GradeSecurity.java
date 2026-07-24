package com.rusobr.academic.config.security;

import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("gradeSecurity")
@RequiredArgsConstructor
public class GradeSecurity {

    private final GradeRepository gradeRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;

    public boolean canViewStudent(Long gradeId, Authentication auth) {
        Long userId = ((Jwt) auth.getPrincipal()).getClaim("user_id");

        return gradeRepository.existsByIdAndStudentId(gradeId, userId);
    }

    public boolean canViewAssignment(Long teachingAssignmentId, Authentication auth) {
        Long userId = ((Jwt) auth.getPrincipal()).getClaim("user_id");
        List<Long> teachingAssignmentIds = teachingAssignmentRepository
                .getTeachingAssignmentIdsByTeacherId(userId);

        return teachingAssignmentIds.contains(teachingAssignmentId);
    }

}

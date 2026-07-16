package com.rusobr.academic.config.security;

import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("gradeSecurity")
@RequiredArgsConstructor
public class GradeSecurity {

    private final GradeRepository gradeRepository;

    public boolean canViewStudent(Long gradeId, Authentication auth) {
        Long userId = ((Jwt) auth.getPrincipal()).getClaim("user_id");

        return gradeRepository.existsByIdAndStudentId(gradeId, userId);
    }

}

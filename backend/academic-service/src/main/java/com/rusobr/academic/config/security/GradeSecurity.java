package com.rusobr.academic.config.security;

import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.academic.infrastructure.persistence.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("gradeSecurity")
@RequiredArgsConstructor
public class GradeSecurity {

    private final GradeRepository gradeRepository;
    private final CurrentStudentContext currentStudentContext;

    public boolean canViewStudent(Long gradeId) {
        return gradeRepository.existsByIdAndStudentId(gradeId, currentStudentContext.getStudentId());
    }

}

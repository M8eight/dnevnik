package com.rusobr.academic.infrastructure.interceptor;

import com.rusobr.academic.config.security.ParentSecurity;
import com.rusobr.common.exception.MissingStudentContextException;
import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.common.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.rusobr.academic.web.exception.AcademicExceptionCode.PARENT_STUDENT_ID_NOT_SELECTED;
import static com.rusobr.academic.web.exception.AcademicExceptionCode.STUDENT_DOES_NOT_ATTACHED_PARENT;

@Component
@RequiredArgsConstructor
public class StudentContextInterceptor implements HandlerInterceptor {

    private final CurrentStudentContext currentStudentContext;
    private final ParentSecurity parentSecurity;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken auth)) {
            return true;
        }
        Long currentUserId = Long.valueOf(auth.getToken().getClaimAsString("user_id"));

        boolean isStudent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        boolean isParent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARENT"));

        if (isStudent) {
            currentStudentContext.setStudentId(currentUserId);
        } else if (isParent) {
            String header = request.getHeader("X-Student-Id");
            if (header == null || header.isBlank()) {
                throw new MissingStudentContextException("studentId not found in header", PARENT_STUDENT_ID_NOT_SELECTED);
            }
            Long requestedStudentId;
            try {
                requestedStudentId = Long.valueOf(header);
            } catch (NumberFormatException e) {
                throw new MissingStudentContextException("invalid studentId header", PARENT_STUDENT_ID_NOT_SELECTED);
            }
            if (!parentSecurity.isChild(currentUserId, requestedStudentId)) {
                throw new ForbiddenException(
                        "Student %d is not a child of parent %d".formatted(requestedStudentId, currentUserId),
                        STUDENT_DOES_NOT_ATTACHED_PARENT
                );
            }
            currentStudentContext.setStudentId(requestedStudentId);
        }
        return true;
    }
}

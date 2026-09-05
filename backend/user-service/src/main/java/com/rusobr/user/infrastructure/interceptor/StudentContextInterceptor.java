package com.rusobr.user.infrastructure.interceptor;

import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.common.exception.MissingStudentContextException;
import com.rusobr.common.exception.ForbiddenException;
import com.rusobr.user.application.service.student.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.rusobr.user.web.exception.UserExceptionCode.PARENT_STUDENT_ID_NOT_SELECTED;
import static com.rusobr.user.web.exception.UserExceptionCode.STUDENT_DOES_NOT_ATTACHED_PARENT;

@Component
public class StudentContextInterceptor implements HandlerInterceptor {

    private final CurrentStudentContext currentStudentContext;
    private final StudentService studentService;

    public StudentContextInterceptor(CurrentStudentContext currentStudentContext,
                                     @Lazy StudentService studentService) {
        this.currentStudentContext = currentStudentContext;
        this.studentService = studentService;
    }

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
            if (!studentService.isChild(currentUserId, requestedStudentId)) {
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

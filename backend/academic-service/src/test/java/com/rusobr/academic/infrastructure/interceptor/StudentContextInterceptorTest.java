package com.rusobr.academic.infrastructure.interceptor;

import com.rusobr.academic.config.security.ParentSecurity;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.common.exception.ForbiddenException;
import com.rusobr.common.exception.MissingStudentContextException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentContextInterceptorTest {

    private static final Long CURRENT_USER_ID = 42L;
    private static final Long REQUESTED_STUDENT_ID = 99L;

    @Mock
    private CurrentStudentContext currentStudentContext;

    @Mock
    private ParentSecurity parentSecurity;

    @InjectMocks
    private StudentContextInterceptor interceptor;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String role) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("user_id", CURRENT_USER_ID)
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role))));
    }

    private void authenticateWithRoles(List<String> roles) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("user_id", CURRENT_USER_ID)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, roles.stream().map(SimpleGrantedAuthority::new).toList()));
    }

    @Test
    @DisplayName("без authentication — запрос пропускается, контекст не заполняется")
    void noAuthentication_passesThrough() {
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(currentStudentContext, never()).setStudentId(any());
    }

    @Test
    @DisplayName("non-JWT authentication — запрос пропускается, контекст не заполняется")
    void nonJwtAuthentication_passesThrough() {
        SecurityContextHolder.getContext().setAuthentication(mock(org.springframework.security.core.Authentication.class));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(currentStudentContext, never()).setStudentId(any());
    }

    @Test
    @DisplayName("роль отличная от STUDENT/PARENT — контекст не заполняется")
    void otherRole_passesThrough() {
        authenticateWithRoles(List.of("ROLE_TEACHER", "ROLE_ADMIN"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(currentStudentContext, never()).setStudentId(any());
    }

    @Test
    @DisplayName("STUDENT — контекст заполняется из user_id токена")
    void student_resolvesContextFromUserId() {
        authenticate("ROLE_STUDENT");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(currentStudentContext).setStudentId(CURRENT_USER_ID);
    }

    @Test
    @DisplayName("PARENT с валидным X-Student-Id своего ребёнка — контекст заполняется")
    void parent_withValidChildHeader_resolvesContext() {
        authenticate("ROLE_PARENT");
        request.addHeader("X-Student-Id", String.valueOf(REQUESTED_STUDENT_ID));
        when(parentSecurity.isChild(CURRENT_USER_ID, REQUESTED_STUDENT_ID)).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(currentStudentContext).setStudentId(REQUESTED_STUDENT_ID);
    }

    @Test
    @DisplayName("PARENT без X-Student-Id — MissingStudentContextException")
    void parent_withoutHeader_throwsMissingStudentContext() {
        authenticate("ROLE_PARENT");

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(MissingStudentContextException.class)
                .hasMessageContaining("studentId not found in header")
                .satisfies(ex -> assertThat(((MissingStudentContextException) ex).getCode())
                        .isEqualTo(AcademicExceptionCode.PARENT_STUDENT_ID_NOT_SELECTED));
        verify(currentStudentContext, never()).setStudentId(any());
    }

    @Test
    @DisplayName("PARENT с пустым X-Student-Id — MissingStudentContextException")
    void parent_withBlankHeader_throwsMissingStudentContext() {
        authenticate("ROLE_PARENT");
        request.addHeader("X-Student-Id", "   ");

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(MissingStudentContextException.class)
                .hasMessageContaining("studentId not found in header");
    }

    @Test
    @DisplayName("PARENT с некорректным X-Student-Id — MissingStudentContextException")
    void parent_withInvalidHeader_throwsMissingStudentContext() {
        authenticate("ROLE_PARENT");
        request.addHeader("X-Student-Id", "abc");

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(MissingStudentContextException.class)
                .hasMessageContaining("invalid studentId header")
                .satisfies(ex -> assertThat(((MissingStudentContextException) ex).getCode())
                        .isEqualTo(AcademicExceptionCode.PARENT_STUDENT_ID_NOT_SELECTED));
        verify(parentSecurity, never()).isChild(any(), any());
    }

    @Test
    @DisplayName("PARENT с чужим студентом — ForbiddenException")
    void parent_withNonChildStudent_throwsForbidden() {
        authenticate("ROLE_PARENT");
        request.addHeader("X-Student-Id", String.valueOf(REQUESTED_STUDENT_ID));
        when(parentSecurity.isChild(CURRENT_USER_ID, REQUESTED_STUDENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not a child")
                .satisfies(ex -> assertThat(((ForbiddenException) ex).getCode())
                        .isEqualTo(AcademicExceptionCode.STUDENT_DOES_NOT_ATTACHED_PARENT));
        verify(currentStudentContext, never()).setStudentId(any());
    }
}
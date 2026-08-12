package com.rusobr.academic.feignIT;

import com.github.tomakehurst.wiremock.http.Fault;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.academic.web.exception.BadRequestException;
import com.rusobr.academic.web.exception.UserServiceUnavailableException;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.common.exception.ForbiddenException;
import com.rusobr.common.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeAllMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserClientIT extends FeignIntegrationTestBase {

    @Autowired
    private UserClient userClient;

    @Test
    void getTeacherSimpleById_shouldReturnDeserializedTeacher_whenFound() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1/simple"))
                .willReturn(okJson("""
                        {
                            "id": 1,
                            "firstName": "Учительница",
                            "lastName": "Учитель",
                            "username": "teacher_soc",
                            "keycloakId": "1234-1234-1234"
                        }
                        """)));

        UserFeignResponse res = userClient.getTeacherSimpleById(1L);

        assertThat(res).isEqualTo(new UserFeignResponse(1L, "Учительница", "Учитель",
                "teacher_soc", "1234-1234-1234"));
    }

    @Test
    void getTeacherSimpleById_shouldCallExactPath_whenIdProvided() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/42/simple"))
                .willReturn(okJson("""
                        { "id": 42, "firstName": "И", "lastName": "П", "username": "u", "keycloakId": "k" }
                        """)));

        UserFeignResponse res = userClient.getTeacherSimpleById(42L);

        assertThat(res.id()).isEqualTo(42L);
        verify(getRequestedFor(urlPathEqualTo("/api/v1/teachers/42/simple")));
    }

    @Test
    void getTeacherSimpleById_shouldThrowBadRequest_whenServiceReturns404() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/99/simple"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> userClient.getTeacherSimpleById(99L))
                .isInstanceOfSatisfying(BadRequestException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_TEACHER_NOT_FOUND));
    }

    @Test
    void getTeacherById_shouldReturnDeserializedTeacherWithNestedFields_whenFound() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1"))
                .willReturn(okJson("""
                        {
                            "user": {
                                "id": 1,
                                "firstName": "Иван",
                                "lastName": "Иванов",
                                "username": "ivan",
                                "keycloakId": "kc-1"
                            },
                            "details": {
                                "email": "ivan@test.ru",
                                "phoneNumber": "+79001234567"
                            }
                        }
                        """)));

        TeacherResponse res = userClient.getTeacherById(1L);

        assertThat(res.user()).isEqualTo(new UserFeignResponse(1L, "Иван", "Иванов", "ivan", "kc-1"));
        assertThat(res.details().email()).isEqualTo("ivan@test.ru");
        assertThat(res.details().phoneNumber()).isEqualTo("+79001234567");
    }

    @Test
    void getTeacherById_shouldThrowBadRequest_whenServiceReturns404() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/99"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> userClient.getTeacherById(99L))
                .isInstanceOfSatisfying(BadRequestException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_TEACHER_NOT_FOUND);
                    assertThat(e.getMessage()).contains("Not found teacher with id: 99");
                });
    }

    @Test
    void getTeacherById_shouldThrowUnauthorized_whenServiceReturns401() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1"))
                .willReturn(aResponse().withStatus(401)));

        assertThatThrownBy(() -> userClient.getTeacherById(1L))
                .isInstanceOfSatisfying(UnauthorizedException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_UNAUTHORIZED));
    }

    @Test
    void getTeacherById_shouldThrowForbidden_whenServiceReturns403() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1"))
                .willReturn(aResponse().withStatus(403)));

        assertThatThrownBy(() -> userClient.getTeacherById(1L))
                .isInstanceOfSatisfying(ForbiddenException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_FORBIDDEN));
    }

    @Test
    void getTeacherById_shouldThrowUnavailable_whenServiceReturns500() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> userClient.getTeacherById(1L))
                .isInstanceOfSatisfying(UserServiceUnavailableException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_UNAVAILABLE));
    }

    @Test
    void getBatchTeachers_shouldSendJsonBodyAndReturnDeserializedBatch_whenFound() {
        stubFor(post(urlPathEqualTo("/api/v1/teachers/batch"))
                .withRequestBody(equalToJson("[1, 2]"))
                .willReturn(okJson("""
                        {
                            "found": [
                                { "id": 1, "firstName": "Иван", "lastName": "Иванов", "username": "ivan", "keycloakId": "kc-1" },
                                { "id": 2, "firstName": "Петр", "lastName": "Петров", "username": "petr", "keycloakId": "kc-2" }
                            ],
                            "notFound": [3],
                            "degraded": false
                        }
                        """)));

        BatchUserResponse res = userClient.getBatchTeachers(List.of(1L, 2L));

        assertThat(res.found()).hasSize(2);
        assertThat(res.found().get(0)).isEqualTo(new UserFeignResponse(1L, "Иван", "Иванов", "ivan", "kc-1"));
        assertThat(res.found().get(1)).isEqualTo(new UserFeignResponse(2L, "Петр", "Петров", "petr", "kc-2"));
        assertThat(res.notFound()).containsExactly(3L);
        assertThat(res.degraded()).isFalse();
        verify(postRequestedFor(urlPathEqualTo("/api/v1/teachers/batch"))
                .withRequestBody(equalToJson("[1, 2]")));
    }

    @Test
    void getBatchTeachers_shouldReturnEmptyBatch_whenServiceReturnsEmptyBatch() {
        stubFor(post(urlPathEqualTo("/api/v1/teachers/batch"))
                .willReturn(okJson("""
                        { "found": [], "notFound": [], "degraded": false }
                        """)));

        BatchUserResponse res = userClient.getBatchTeachers(List.of());

        assertThat(res.found()).isEmpty();
        assertThat(res.notFound()).isEmpty();
        assertThat(res.degraded()).isFalse();
    }

    @Test
    void getBatchTeachers_shouldThrowBadRequest_whenServiceReturns404() {
        stubFor(post(urlPathEqualTo("/api/v1/teachers/batch"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> userClient.getBatchTeachers(List.of(1L)))
                .isInstanceOfSatisfying(BadRequestException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_BATCH_TEACHERS_NOT_FOUND));
    }

    @Test
    void existStudentById_shouldNotThrow_whenServiceReturns200() {
        stubFor(get(urlPathEqualTo("/api/v1/students/5/details"))
                .willReturn(ok()));

        assertThatCode(() -> userClient.existStudentById(5L)).doesNotThrowAnyException();
        verify(getRequestedFor(urlPathEqualTo("/api/v1/students/5/details")));
    }

    @Test
    void existStudentById_shouldThrowBadRequest_whenServiceReturns404() {
        stubFor(get(urlPathEqualTo("/api/v1/students/5/details"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> userClient.existStudentById(5L))
                .isInstanceOfSatisfying(BadRequestException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_STUDENT_NOT_FOUND));
    }

    @Test
    void getBatchStudentsExcludeAssigned_shouldSendJsonBodyAndReturnStudents_whenFound() {
        stubFor(post(urlPathEqualTo("/api/v1/students/exclude-assigned"))
                .withRequestBody(equalToJson("[1, 2]", true, false))
                .willReturn(okJson("""
                        [
                            { "id": 1, "firstName": "Иван", "lastName": "Иванов", "username": "ivan", "keycloakId": "kc-1" }
                        ]
                        """)));

        Set<Long> ids = new LinkedHashSet<>(List.of(1L, 2L));

        List<UserFeignResponse> res = userClient.getBatchStudentsExcludeAssigned(ids);

        assertThat(res).containsExactly(new UserFeignResponse(1L, "Иван", "Иванов", "ivan", "kc-1"));
        verify(postRequestedFor(urlPathEqualTo("/api/v1/students/exclude-assigned"))
                .withRequestBody(equalToJson("[1, 2]", true, false)));
    }

    @Test
    void getBatchStudentsExcludeAssigned_shouldReturnEmptyList_whenServiceReturnsEmptyArray() {
        stubFor(post(urlPathEqualTo("/api/v1/students/exclude-assigned"))
                .willReturn(okJson("[]")));

        assertThat(userClient.getBatchStudentsExcludeAssigned(Set.of())).isEmpty();
    }

    @Test
    void getBatchStudentsExcludeAssigned_shouldThrowBadRequest_whenServiceReturns404() {
        stubFor(post(urlPathEqualTo("/api/v1/students/exclude-assigned"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> userClient.getBatchStudentsExcludeAssigned(Set.of(1L)))
                .isInstanceOfSatisfying(BadRequestException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_BATCH_EXCLUDE_STUDENTS_NOT_FOUND));
    }

    @Test
    void getBatchStudents_shouldSendJsonBodyAndReturnDeserializedBatch_whenFound() {
        stubFor(post(urlPathEqualTo("/api/v1/students/batch"))
                .withRequestBody(equalToJson("[7]"))
                .willReturn(okJson("""
                        {
                            "found": [ { "id": 7, "firstName": "Оля", "lastName": "Орлова", "username": "ola", "keycloakId": "kc-7" } ],
                            "notFound": [],
                            "degraded": false
                        }
                        """)));

        BatchUserResponse res = userClient.getBatchStudents(List.of(7L));

        assertThat(res.found()).containsExactly(new UserFeignResponse(7L, "Оля", "Орлова", "ola", "kc-7"));
        assertThat(res.notFound()).isEmpty();
        assertThat(res.degraded()).isFalse();
        verify(postRequestedFor(urlPathEqualTo("/api/v1/students/batch"))
                .withRequestBody(equalToJson("[7]")));
    }

    @Test
    void getBatchStudents_shouldThrowBadRequest_whenServiceReturns404() {
        stubFor(post(urlPathEqualTo("/api/v1/students/batch"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> userClient.getBatchStudents(List.of(1L)))
                .isInstanceOfSatisfying(BadRequestException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_BATCH_USERS_NOT_FOUND));
    }

    @Test
    void getBatchStudents_shouldReturnDegradedBatch_whenServiceReturns500() {
        stubFor(post(urlPathEqualTo("/api/v1/students/batch"))
                .willReturn(aResponse().withStatus(500)));

        BatchUserResponse res = userClient.getBatchStudents(List.of(1L, 2L));

        assertThat(res.degraded()).isTrue();
        assertThat(res.found()).extracting(UserFeignResponse::id).containsExactly(1L, 2L);
        assertThat(res.found()).allMatch(u -> u.firstName() == null);
        assertThat(res.notFound()).isEmpty();
    }

    @Test
    void getTeacherById_shouldThrowUnavailable_whenConnectionResetByPeer() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> userClient.getTeacherById(1L))
                .isInstanceOfSatisfying(UserServiceUnavailableException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_UNAVAILABLE));
    }

    @Test
    void getTeacherById_shouldThrowUnavailable_whenResponseExceedsReadTimeout() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1"))
                .willReturn(okJson("""
                        { "user": null, "details": null }
                        """).withFixedDelay(2000)));

        assertThatThrownBy(() -> userClient.getTeacherById(1L))
                .isInstanceOfSatisfying(UserServiceUnavailableException.class, e ->
                        assertThat(e.getCode()).isEqualTo(AcademicExceptionCode.USER_SERVICE_UNAVAILABLE));
    }

    @Test
    void feignCall_shouldPropagateAuthHeaderFromSecurityContext() {
        Jwt jwt = Jwt.withTokenValue("incoming")
                .header("alg", "RS256")
                .claim("sub", "teacher-1")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

        stubFor(post(urlPathEqualTo("/api/v1/students/batch"))
                .willReturn(okJson("""
                        { "found": [], "notFound": [], "degraded": false }
                        """)));

        userClient.getBatchStudents(List.of(1L));

        verify(postRequestedFor(urlPathEqualTo("/api/v1/students/batch"))
                .withHeader("Authorization", equalTo("Bearer incoming")));
    }

    @Test
    void feignCall_shouldNotSendAuthorizationHeader_whenSecurityContextEmpty() {
        stubFor(post(urlPathEqualTo("/api/v1/students/batch"))
                .willReturn(okJson("""
                        { "found": [], "notFound": [], "degraded": false }
                        """)));

        userClient.getBatchStudents(List.of(1L));

        verify(postRequestedFor(urlPathEqualTo("/api/v1/students/batch"))
                .withoutHeader("Authorization"));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        removeAllMappings();
        reset();
    }
}

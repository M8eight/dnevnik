package com.rusobr.user.feignIT;

import com.github.tomakehurst.wiremock.http.Fault;
import com.rusobr.common.exception.ForbiddenException;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.common.exception.UnauthorizedException;
import com.rusobr.user.infrastructure.client.feign.AcademicClient;
import com.rusobr.user.web.dto.feign.AcademicYearResponse;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.feign.TeacherAcademicFeignDto;
import com.rusobr.user.web.exception.AcademicServiceUnavailableException;
import com.rusobr.user.web.exception.UserExceptionCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

public class AcademicClientIT extends FeignIntegrationTestBase {

    @Autowired
    private AcademicClient academicClient;

    @Test
    void getSchoolClassByStudentId_shouldReturnDeserializedSchoolClass_whenFound() {
        stubFor(get(urlPathEqualTo("/api/v1/school-classes/search/by-student"))
                .withQueryParam("studentId", equalTo("1"))
                .willReturn(okJson("""
                        {
                            "id": 1,
                            "name": "8A",
                            "academicYear": {
                              "id": 1,
                              "name": "2025-2026",
                              "description": "123",
                              "startDate": "2025-04-30",
                              "endDate": "2025-05-30",
                              "isActive": true
                            },
                            "classTeacherId": 27
                        }
                        """)));

        SchoolClassResponse res = academicClient.getSchoolClassByStudentId(1L);

        assertThat(res).isEqualTo(new SchoolClassResponse(1L, "8A", new AcademicYearResponse(1L, "2025-2026", "123",
                LocalDate.of(2025, 4, 30), LocalDate.of(2025, 5, 30), true), 27L));
    }

    @Test
    void getSchoolClassByStudentId_shouldSendCorrectQueryParam_whenIdProvided() {
        stubFor(get(urlPathEqualTo("/api/v1/school-classes/search/by-student"))
                .willReturn(okJson("""
                        {
                            "id": 1,
                            "name": "8A",
                            "academicYear": {
                              "id": 1,
                              "name": "2025-2026",
                              "description": "123",
                              "startDate": "2025-04-30",
                              "endDate": "2025-05-30",
                              "isActive": true
                            },
                            "classTeacherId": 27
                        }
                        """)));

        SchoolClassResponse res = academicClient.getSchoolClassByStudentId(42L);

        assertThat(res.id()).isEqualTo(1);
        verify(getRequestedFor(urlPathEqualTo("/api/v1/school-classes/search/by-student"))
                .withQueryParam("studentId", equalTo("42")));
    }

    @Test
    void getSchoolClassByStudentId_shouldThrowNotFound_whenServiceReturns404() {
        stubFor(get(urlPathEqualTo("/api/v1/school-classes/search/by-student"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> academicClient.getSchoolClassByStudentId(99L))
                .isInstanceOfSatisfying(NotFoundException.class, e ->
                        assertThat(e.getCode()).isEqualTo(UserExceptionCode.SCHOOL_CLASS_BY_STUDENT_NOT_FOUND));
    }

    @Test
    void getSchoolClassByStudentId_shouldThrowUnauthorized_whenServiceReturns401() {
        stubFor(get(urlPathEqualTo("/api/v1/school-classes/search/by-student"))
                .willReturn(aResponse().withStatus(401)));

        assertThatThrownBy(() -> academicClient.getSchoolClassByStudentId(1L))
                .isInstanceOfSatisfying(UnauthorizedException.class, e ->
                        assertThat(e.getCode()).isEqualTo(UserExceptionCode.ACADEMIC_SERVICE_UNAUTHORIZED));
    }

    @Test
    void getSchoolClassByStudentId_shouldThrowForbidden_whenServiceReturns403() {
        stubFor(get(urlPathEqualTo("/api/v1/school-classes/search/by-student"))
                .willReturn(aResponse().withStatus(403)));

        assertThatThrownBy(() -> academicClient.getSchoolClassByStudentId(1L))
                .isInstanceOfSatisfying(ForbiddenException.class, e ->
                        assertThat(e.getCode()).isEqualTo(UserExceptionCode.ACADEMIC_SERVICE_FORBIDDEN));
    }

    @Test
    void getSchoolClassByStudentId_shouldThrowUnavailable_whenConnectionResetByPeer() {
        stubFor(get(urlPathEqualTo("/api/v1/school-classes/search/by-student"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> academicClient.getSchoolClassByStudentId(1L))
                .isInstanceOfSatisfying(AcademicServiceUnavailableException.class, e ->
                        assertThat(e.getCode()).isEqualTo(UserExceptionCode.ACADEMIC_SERVICE_UNAVAILABLE));
    }

    @Test
    void getTeacherAcademicInfo_shouldReturnDeserializedInfo_whenFound() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1/info"))
                .willReturn(okJson("""
                        {
                            "subjects": [
                                { "subject": { "id": 10, "name": "Математика" } }
                            ],
                            "classes": [
                                {
                                    "id": 1,
                                    "name": "8A",
                                    "academicYear": {
                                        "id": 1,
                                        "name": "2025-2026",
                                        "description": "123",
                                        "startDate": "2025-04-30",
                                        "endDate": "2025-05-30",
                                        "isActive": true
                                    },
                                    "classTeacherId": 27
                                }
                            ],
                            "assignments": [
                                {
                                    "id": 5,
                                    "subject": { "id": 10, "name": "Математика" },
                                    "schoolClass": {
                                        "id": 1,
                                        "name": "8A",
                                        "academicYear": {
                                            "id": 1,
                                            "name": "2025-2026",
                                            "description": "123",
                                            "startDate": "2025-04-30",
                                            "endDate": "2025-05-30",
                                            "isActive": true
                                        },
                                        "classTeacherId": 27
                                    }
                                }
                            ]
                        }
                        """)));

        TeacherAcademicFeignDto res = academicClient.getTeacherAcademicInfo(1L);

        assertThat(res.subjects()).hasSize(1);
        assertThat(res.subjects().get(0).subject().name()).isEqualTo("Математика");
        assertThat(res.classes()).hasSize(1);
        assertThat(res.classes().get(0).name()).isEqualTo("8A");
        assertThat(res.assignments()).hasSize(1);
        assertThat(res.assignments().get(0).id()).isEqualTo(5L);
        assertThat(res.assignments().get(0).schoolClass().classTeacherId()).isEqualTo(27L);

        verify(getRequestedFor(urlPathEqualTo("/api/v1/teachers/1/info")));
    }

    @Test
    void getTeacherAcademicInfo_shouldThrowNotFound_whenServiceReturns404() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/99/info"))
                .willReturn(aResponse().withStatus(404)));

        assertThatThrownBy(() -> academicClient.getTeacherAcademicInfo(99L))
                .isInstanceOfSatisfying(NotFoundException.class, e ->
                        assertThat(e.getCode()).isEqualTo(UserExceptionCode.ACADEMIC_SERVICE_TEACHER_INFO_NOT_FOUND));
    }

    @Test
    void getTeacherAcademicInfo_shouldThrowUnauthorized_whenServiceReturns401() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1/info"))
                .willReturn(aResponse().withStatus(401)));

        assertThatThrownBy(() -> academicClient.getTeacherAcademicInfo(1L))
                .isInstanceOfSatisfying(UnauthorizedException.class, e ->
                        assertThat(e.getCode()).isEqualTo(UserExceptionCode.ACADEMIC_SERVICE_UNAUTHORIZED));
    }

    @Test
    void getTeacherAcademicInfo_shouldThrowForbidden_whenServiceReturns403() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1/info"))
                .willReturn(aResponse().withStatus(403)));

        assertThatThrownBy(() -> academicClient.getTeacherAcademicInfo(1L))
                .isInstanceOfSatisfying(ForbiddenException.class, e ->
                        assertThat(e.getCode()).isEqualTo(UserExceptionCode.ACADEMIC_SERVICE_FORBIDDEN));
    }

    @Test
    void getTeacherAcademicInfo_shouldThrowUnavailable_whenResponseExceedsReadTimeout() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1/info"))
                .willReturn(okJson("""
                        { "subjects": [], "classes": [], "assignments": [] }
                        """).withFixedDelay(2000)));

        assertThatThrownBy(() -> academicClient.getTeacherAcademicInfo(1L))
                .isInstanceOfSatisfying(AcademicServiceUnavailableException.class, e ->
                        assertThat(e.getCode()).isEqualTo(UserExceptionCode.ACADEMIC_SERVICE_UNAVAILABLE));
    }

    @Test
    void feignCall_shouldPropagateAuthHeaderFromSecurityContext() {
        Jwt jwt = Jwt.withTokenValue("incoming")
                .header("alg", "RS256")
                .claim("sub", "teacher-1")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

        stubFor(get(urlPathEqualTo("/api/v1/teachers/1/info"))
                .willReturn(okJson("""
                        { "subjects": [], "classes": [], "assignments": [] }
                        """)));

        academicClient.getTeacherAcademicInfo(1L);

        verify(getRequestedFor(urlPathEqualTo("/api/v1/teachers/1/info"))
                .withHeader("Authorization", equalTo("Bearer incoming")));
    }

    @Test
    void feignCall_shouldNotSendAuthorizationHeader_whenSecurityContextEmpty() {
        stubFor(get(urlPathEqualTo("/api/v1/teachers/1/info"))
                .willReturn(okJson("""
                        { "subjects": [], "classes": [], "assignments": [] }
                        """)));

        academicClient.getTeacherAcademicInfo(1L);

        verify(getRequestedFor(urlPathEqualTo("/api/v1/teachers/1/info"))
                .withoutHeader("Authorization"));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        removeAllMappings();
        reset();
    }
}
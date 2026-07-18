package com.rusobr.user.infrastructure.client.feign;

import com.rusobr.common.exception.ForbiddenException;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.common.exception.UnauthorizedException;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.feign.TeacherAcademicFeignDto;
import com.rusobr.user.web.exception.*;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AcademicClientFallbackFactory implements FallbackFactory<AcademicClient> {

    @Override
    public AcademicClient create(Throwable cause) {
        return new AcademicClient() {

            @Override
            public SchoolClassResponse getSchoolClassByStudentId(Long studentId) {
                handleCommonErrors(
                        cause,
                        "getSchoolClassByStudentId",
                        "School class with %s not found".formatted(studentId),
                        UserExceptionCode.SCHOOL_CLASS_BY_STUDENT_NOT_FOUND
                );
                throw fallbackFailure("getSchoolClassByStudentId", studentId, cause);
            }

            @Override
            public TeacherAcademicFeignDto getTeacherAcademicInfo(Long teacherId) {
                handleCommonErrors(
                        cause,
                        "getTeacherAcademicInfo",
                        "Teacher info with id: %s not found".formatted(teacherId),
                        UserExceptionCode.ACADEMIC_SERVICE_TEACHER_INFO_NOT_FOUND
                );
                throw fallbackFailure("getTeacherAcademicInfo", teacherId, cause);
            }
        };
    }

    private void handleCommonErrors(Throwable cause, String context, String notFoundMessage, UserExceptionCode notFoundCode) {
        if (notFoundCode != null && cause instanceof FeignException.NotFound) {
            throw new NotFoundException(notFoundMessage, notFoundCode);
        }

        if (cause instanceof FeignException.Unauthorized) {
            log.error("Fallback [{}]: academic-service Unauthorized", context, cause);
            throw new UnauthorizedException("Не авторизован для запроса к academic-service", UserExceptionCode.ACADEMIC_SERVICE_UNAUTHORIZED);
        }

        if (cause instanceof FeignException.Forbidden) {
            log.error("Fallback [{}]: academic-service Forbidden", context, cause);
            throw new ForbiddenException("Недостаточно прав для запроса к academic-service", UserExceptionCode.ACADEMIC_SERVICE_FORBIDDEN);
        }
    }

    private AcademicServiceUnavailableException fallbackFailure(String method, Object arg, Throwable cause) {
        log.error("Fallback: {} arg={}", method, arg, cause);
        return new AcademicServiceUnavailableException(
                "Не удалось выполнить запрос к academic-service: (%s) %s".formatted(method, arg),
                cause,
                UserExceptionCode.ACADEMIC_SERVICE_UNAVAILABLE
        );
    }
}
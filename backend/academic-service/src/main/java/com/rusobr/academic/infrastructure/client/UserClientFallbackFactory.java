package com.rusobr.academic.infrastructure.client;

import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.academic.web.exception.BadRequestException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.exception.ForbiddenException;
import com.rusobr.common.exception.UnauthorizedException;
import com.rusobr.common.util.ExceptionUtils;
import com.rusobr.academic.web.exception.UserServiceUnavailableException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        Throwable rootCause = ExceptionUtils.unwrapRootCause(cause);
        return new UserClient() {

            @Override
            public TeacherResponse getTeacherById(Long id) {
                handleCommonErrors(rootCause, "getTeacherById id=" + id,
                        "Not found teacher with id: %d".formatted(id),
                        AcademicExceptionCode.USER_SERVICE_TEACHER_NOT_FOUND);
                throw fallbackFailure("getTeacherById", id, rootCause);
            }

            @Override
            public UserFeignResponse getTeacherSimpleById(Long id) {
                handleCommonErrors(rootCause, "getTeacherSimpleById id=" + id,
                        "Not found teacher with id: %d".formatted(id),
                        AcademicExceptionCode.USER_SERVICE_TEACHER_NOT_FOUND);
                throw fallbackFailure("getTeacherSimpleById", id, rootCause);
            }

            @Override
            public BatchUserResponse getBatchTeachers(List<Long> ids) {
                handleCommonErrors(rootCause, "getBatchTeachers ids=" + ids, "Not found batch teachers with ids: %s".formatted(ids),
                        AcademicExceptionCode.USER_SERVICE_BATCH_TEACHERS_NOT_FOUND);
                throw fallbackFailure("getBatchTeachers", ids, rootCause);
            }

            @Override
            public void existStudentById(Long id) {
                handleCommonErrors(rootCause, "existStudentById id=" + id,
                        "Not found student with id: %d".formatted(id),
                        AcademicExceptionCode.USER_SERVICE_STUDENT_NOT_FOUND);
                throw fallbackFailure("existStudentById", id, rootCause);
            }

            @Override
            public List<UserFeignResponse> getBatchStudentsExcludeAssigned(Set<Long> ids) {
                handleCommonErrors(rootCause, "getBatchStudentsExcludeAssigned ids=" + ids, "Not found batch exclude students with ids: %s".formatted(ids),
                        AcademicExceptionCode.USER_SERVICE_BATCH_EXCLUDE_STUDENTS_NOT_FOUND);
                throw fallbackFailure("getBatchStudentsExcludeAssigned", ids, rootCause);
            }

            @Override
            public BatchUserResponse getBatchStudents(List<Long> ids) {
                handleCommonErrors(rootCause, "getBatchUsers ids=" + ids, "Not found batch users with ids: %s".formatted(ids),
                        AcademicExceptionCode.USER_SERVICE_BATCH_USERS_NOT_FOUND);
                return BatchUserResponse.degraded(ids);
            }
        };
    }

    private void handleCommonErrors(Throwable cause, String context, String notFoundMessage, AcademicExceptionCode notFoundCode) {
        if (notFoundCode != null && cause instanceof FeignException.NotFound) {
            throw new BadRequestException(notFoundMessage, notFoundCode);
        }
        if (cause instanceof FeignException.Unauthorized) {
            log.error("Fallback [{}]: user-service Unauthorized", context, cause);
            throw new UnauthorizedException("Не авторизован для запроса к user-service", AcademicExceptionCode.USER_SERVICE_UNAUTHORIZED);
        }
        if (cause instanceof FeignException.Forbidden) {
            log.error("Fallback [{}]: user-service Forbidden", context, cause);
            throw new ForbiddenException("Недостаточно прав для запроса к user-service", AcademicExceptionCode.USER_SERVICE_FORBIDDEN);
        }
    }

    private UserServiceUnavailableException fallbackFailure(String method, Object arg, Throwable cause) {
        log.error("Fallback: {} arg={}", method, arg, cause);
        return new UserServiceUnavailableException(
                "Не удалось выполнить запрос к user-service: (%s) %s".formatted(method, arg),
                cause,
                AcademicExceptionCode.USER_SERVICE_UNAVAILABLE
        );
    }
}
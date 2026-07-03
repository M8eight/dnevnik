package com.rusobr.academic.infrastructure.client;

import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.exception.BadRequestException;
import com.rusobr.academic.web.exception.ExceptionCode;
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
        return new UserClient() {

            @Override
            public TeacherResponse getTeacherById(Long id) {
                if (cause instanceof FeignException.NotFound) {
                    throw new BadRequestException("Not found teacher with id: %d".formatted(id), ExceptionCode.USER_SERVICE_TEACHER_NOT_FOUND);
                }
                log.error("Fallback: getTeacherById id={}", id, cause);
                throw new UserServiceUnavailableException("Не удалось получить учителя id=" + id, cause,
                        ExceptionCode.USER_SERVICE_UNAVAILABLE);
            }

            @Override
            public UserFeignResponse getTeacherSimpleById(Long id) {
                if (cause instanceof FeignException.NotFound) {
                    throw new BadRequestException("Not found teacher with id: %d" + id, ExceptionCode.USER_SERVICE_TEACHER_NOT_FOUND);
                }
                log.error("Fallback: getTeacherSimpleById id={}", id, cause);
                throw new UserServiceUnavailableException("Не удалось получить учителя id: " + id, cause,
                        ExceptionCode.USER_SERVICE_UNAVAILABLE);
            }

            @Override
            public BatchUserResponse getBatchTeachers(List<Long> ids) {
                log.error("Fallback: getBatchTeachers ids={}", ids);
                throw new UserServiceUnavailableException("Не удалось получить batch учителей id: %s".formatted(ids), cause,
                        ExceptionCode.USER_SERVICE_UNAVAILABLE);
            }

            @Override
            public void existStudentById(Long id) {
                if (cause instanceof FeignException.NotFound) {
                    throw new BadRequestException("Not found student with id: %d".formatted(id), ExceptionCode.USER_SERVICE_STUDENT_NOT_FOUND);
                }
                log.error("Fallback: existStudentById id={}", id, cause);
                throw new UserServiceUnavailableException("Не удалось проверить существование студента id: " + id, cause,
                        ExceptionCode.USER_SERVICE_UNAVAILABLE);
            }

            @Override
            public List<UserFeignResponse> getBatchStudentsExcludeAssigned(Set<Long> ids) {
                log.error("Fallback: getBatchStudentsExcludeAssigned");
                throw new UserServiceUnavailableException("Не удалось получить непривязанных студентов id: %s".formatted(ids), cause,
                        ExceptionCode.USER_SERVICE_UNAVAILABLE);
            }

            @Override
            public BatchUserResponse getBatchUsers(List<Long> ids) {
                log.error("Fallback: getBatchUsers ids={}", ids, cause);
                throw new UserServiceUnavailableException("Не удалось получить batch пользователей id: %s".formatted(ids), cause,
                        ExceptionCode.USER_SERVICE_UNAVAILABLE);
            }

        };
    }
}

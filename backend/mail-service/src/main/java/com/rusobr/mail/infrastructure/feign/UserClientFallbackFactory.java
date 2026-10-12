package com.rusobr.mail.infrastructure.feign;

import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.common.exception.ForbiddenException;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.common.exception.UnauthorizedException;
import com.rusobr.common.util.ExceptionUtils;
import com.rusobr.mail.web.exception.MailExceptionCode;
import com.rusobr.mail.web.exception.UserServiceUnavailableException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        Throwable rootCause = ExceptionUtils.unwrapRootCause(cause);
        return new UserClient() {

            @Override
            public BatchUserResponse getBatchUsers(List<Long> ids) {
                handleCommonErrors(rootCause, "getBatchUsers ids=" + ids, "Not found batch users with ids: %s".formatted(ids),
                        MailExceptionCode.USER_SERVICE_BATCH_USERS_NOT_FOUND);
                return BatchUserResponse.degraded(ids);
            }

            @Override
            public void existBatchUsers(List<Long> ids) {
                handleCommonErrors(rootCause, "existBatchUsers ids=" + ids, "Not found exist users with ids: %s".formatted(ids),
                        MailExceptionCode.USER_SERVICE_BATCH_USERS_NOT_FOUND);
            }

            @Override
            public UserFeignResponse getUserById(Long id) {
                handleCommonErrors(rootCause, "getUserById id=" + id, "Not found user with id: %s".formatted(id),
                        MailExceptionCode.USER_SERVICE_USER_NOT_FOUND);
                throw fallbackFailure("getUserById", id, rootCause);
            }
        };
    }

    private void handleCommonErrors(Throwable cause, String context, String notFoundMessage, MailExceptionCode notFoundCode) {
        if (notFoundCode != null && cause instanceof FeignException.NotFound) {
            throw new NotFoundException(notFoundMessage, notFoundCode);
        }

        if (cause instanceof FeignException.Unauthorized) {
            log.error("Fallback [{}]: user-service Unauthorized", context, cause);
            throw new UnauthorizedException("Не авторизован для запроса к user-service", MailExceptionCode.USER_SERVICE_UNAUTHORIZED);
        }

        if (cause instanceof FeignException.Forbidden) {
            log.error("Fallback [{}]: user-service Forbidden", context, cause);
            throw new ForbiddenException("Недостаточно прав для запроса к user-service", MailExceptionCode.USER_SERVICE_FORBIDDEN);
        }
    }

    private UserServiceUnavailableException fallbackFailure(String method, Object arg, Throwable cause) {
        log.error("Fallback: {} arg={}", method, arg, cause);
        return new UserServiceUnavailableException(
                "Не удалось выполнить запрос к user-service: (%s) %s".formatted(method, arg),
                cause,
                MailExceptionCode.USER_SERVICE_UNAVAILABLE
        );
    }

}
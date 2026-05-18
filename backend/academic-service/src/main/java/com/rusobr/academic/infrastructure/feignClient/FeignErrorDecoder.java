package com.rusobr.academic.infrastructure.feignClient;

import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new NotFoundException("Feign not found error");
            case 409 -> new ConflictException("Feign conflict error");
            default -> new Default().decode(methodKey, response);
        };
    }
}
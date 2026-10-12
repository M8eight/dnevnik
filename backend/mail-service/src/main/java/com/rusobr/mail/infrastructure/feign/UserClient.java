package com.rusobr.mail.infrastructure.feign;

import com.rusobr.common.config.FeignConfig;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "user-service", fallbackFactory = UserClientFallbackFactory.class,
        configuration = FeignConfig.class, url = "${clients.user-service.url:}")
public interface UserClient {

    @PostMapping("/api/v1/users/batch")
    BatchUserResponse getBatchUsers(@RequestBody List<Long> ids);

    @PostMapping("/api/v1/users/batch/exists")
    void existBatchUsers(@RequestBody List<Long> ids);

    @GetMapping("/api/v1/users/{id}")
    UserFeignResponse getUserById(@PathVariable Long id);

}

package com.rusobr.academic.infrastructure.feignClient;

import com.rusobr.academic.web.dto.userService.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@FeignClient(value = "user-service")
public interface UserClient {

    @PostMapping("/api/v1/users/batch")
    Set<UserResponse> getBatchUsers(@RequestBody Set<Long> ids);


}

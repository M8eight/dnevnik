package com.rusobr.academic.infrastructure.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "user-service")
public interface UserFeignClient {


}

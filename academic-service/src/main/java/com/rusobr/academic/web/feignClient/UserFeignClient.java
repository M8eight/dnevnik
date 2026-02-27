package com.rusobr.academic.web.feignClient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("user-service")
public interface UserFeignClient {

}

package com.rusobr.academic.config;

import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignCircuitBreakerConfiguration {
    @Bean
    CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return ((feignClientName, target, method) -> feignClientName);
    }
}

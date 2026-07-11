package com.rusobr.academic.config;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.ExecutorService;

@Configuration
public class FeignCircuitBreakerConfiguration {
    @Bean
    CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return ((feignClientName, target, method) -> feignClientName);
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerExecutorCustomizer() {
        return factory -> {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(10);
            executor.setMaxPoolSize(30);
            executor.setQueueCapacity(100);
            executor.setThreadNamePrefix("Resilience4j-");
            executor.initialize();

            ExecutorService jExecutorService = executor.getThreadPoolExecutor();

            ExecutorService decoratedService = new DelegatingSecurityContextExecutorService(jExecutorService);

            factory.configureExecutorService(decoratedService);
        };
    }
}

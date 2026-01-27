package com.rusobr.gateway;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            System.out.println(">>> Request: " + exchange.getRequest().getMethod() +
                    " " + exchange.getRequest().getURI());
            System.out.println(">>> Headers: " + exchange.getRequest().getHeaders());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("<<< Response: " + exchange.getResponse().getStatusCode());
            }));
        };
    }
}
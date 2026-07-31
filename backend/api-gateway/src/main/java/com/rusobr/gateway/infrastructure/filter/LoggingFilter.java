package com.rusobr.gateway.infrastructure.filter;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long start = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest();

        return exchange.getPrincipal()
                .cast(Authentication.class)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(jwt -> jwt.getClaimAsString("user_id"))
                .defaultIfEmpty("anonymous")
                .flatMap(userId -> {

                    log.info(
                            "Incoming request",
                            StructuredArguments.keyValue("userId", userId),
                            StructuredArguments.keyValue("method", request.getMethod().name()),
                            StructuredArguments.keyValue("path", request.getPath().value()),
                            StructuredArguments.keyValue("query", request.getURI().getQuery()),
                            StructuredArguments.keyValue("remoteIp",
                                    request.getRemoteAddress() != null
                                            ? request.getRemoteAddress().getAddress().getHostAddress()
                                            : null)
                    );

                    return chain.filter(exchange)
                            .doFinally(signal -> {

                                log.info(
                                        "Outgoing response",
                                        StructuredArguments.keyValue("userId", userId),
                                        StructuredArguments.keyValue("method", request.getMethod().name()),
                                        StructuredArguments.keyValue("path", request.getPath().value()),
                                        StructuredArguments.keyValue("status",
                                                exchange.getResponse().getStatusCode() != null
                                                        ? exchange.getResponse().getStatusCode().value()
                                                        : 0),
                                        StructuredArguments.keyValue("durationMs",
                                                System.currentTimeMillis() - start)
                                );
                            });
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
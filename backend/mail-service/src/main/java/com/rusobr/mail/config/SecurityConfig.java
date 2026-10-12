package com.rusobr.mail.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static com.rusobr.common.config.SecurityHelper.keycloakRoleJwtConverter;
import static com.rusobr.common.enums.UserRole.*;
import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> {
                    req
                            .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                            .requestMatchers(
                                    "/public/**",
                                    "/actuator/**",
                                    "/v3/api-docs/**",
                                    "/swagger-ui/**"
                            ).permitAll()

                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                            .requestMatchers(GET, "/api/v1/mails/**")
                            .hasAnyRole(STUDENT.name(), TEACHER.name(), PARENT.name(), ADMIN.name())

                            .requestMatchers(POST, "/api/v1/mails/**")
                            .hasAnyRole(STUDENT.name(), TEACHER.name(), PARENT.name(), ADMIN.name())

                            .requestMatchers(PATCH, "/api/v1/mails/**")
                            .hasAnyRole(STUDENT.name(), TEACHER.name(), PARENT.name(), ADMIN.name())

                            .requestMatchers(DELETE, "/api/v1/mails/**")
                            .hasAnyRole(STUDENT.name(), TEACHER.name(), PARENT.name(), ADMIN.name())

                            .anyRequest().denyAll();
                })
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(keycloakRoleJwtConverter())
                        )
                );

        return http.build();
    }
}
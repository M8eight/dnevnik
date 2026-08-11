package com.rusobr.user.config;

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
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> {
                    req
                            .requestMatchers("/public/**", "/actuator/**",
                                    "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                            .requestMatchers(POST, "/api/v1/students/batch").hasAnyRole(TEACHER.name(), ADMIN.name(), STUDENT.name())
                            .requestMatchers(GET, "/api/v1/teachers/*/simple").hasAnyRole(ADMIN.name(), STUDENT.name(), TEACHER.name())
                            .requestMatchers(GET, "/api/v1/teachers/*").hasAnyRole(ADMIN.name(), TEACHER.name(), STUDENT.name())
                            .requestMatchers(GET, "/api/v1/users/*").hasRole(TEACHER.name())
                            .requestMatchers(GET, "/api/v1/students/*/details").hasAnyRole(ADMIN.name(), TEACHER.name())
                            .requestMatchers(GET, "/api/v1/parents/*/details").hasAnyRole(ADMIN.name(), TEACHER.name())
                            .requestMatchers(GET, "/api/v1/teachers/*/details").hasAnyRole(ADMIN.name(), TEACHER.name())
                            .requestMatchers(GET, "/api/v1/students/*/info").hasAnyRole(ADMIN.name(), TEACHER.name())
                            .requestMatchers(GET, "/api/v1/students/*").hasAnyRole(ADMIN.name(), TEACHER.name(), STUDENT.name())
                            .requestMatchers(POST, "/api/v1/teachers/batch").hasAnyRole(ADMIN.name(), STUDENT.name())

                            //ADMIN SCOPE
                            .requestMatchers(GET, "/api/v1/users").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/users/students").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/users/parents").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/users/teachers").hasRole(ADMIN.name())
                            .requestMatchers(DELETE, "/api/v1/users/*").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/students/exclude-assigned").hasRole(ADMIN.name())

                            .anyRequest().denyAll();
                })
                .oauth2ResourceServer(oauth2 -> {
                    oauth2.jwt(jwt-> {
                        jwt.jwtAuthenticationConverter(keycloakRoleJwtConverter());
                    });
                });

        return http.build();
    }

}

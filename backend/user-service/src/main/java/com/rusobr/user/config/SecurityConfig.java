package com.rusobr.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.rusobr.user.domain.enums.UserRole.*;
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
                            .requestMatchers("/public/**", "/actuator/health").permitAll()
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                            .requestMatchers(POST, "/api/v1/students/batch").hasAnyRole(TEACHER.name(), ADMIN.name())

                            //STUDENT SCOPE
                            .requestMatchers(GET, "/api/v1/students/with-class").hasRole(STUDENT.name())

                            //ADMIN SCOPE
                            .requestMatchers(GET, "/api/v1/users").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/users/students").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/users/parents").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/users/teachers").hasRole(ADMIN.name())
                            .requestMatchers(DELETE, "/api/v1/users/*").hasRole(ADMIN.name())
                            .requestMatchers(PUT, "/api/v1/users/*").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/students/exclude-assigned").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/students/*/details").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/parents/*/details").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/teachers/*/details").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/teachers/*").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/teachers/batch").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/teachers/*/simple").hasRole(ADMIN.name())

                            .anyRequest().denyAll();
                })
                .oauth2ResourceServer(oauth2 -> {
                    oauth2.jwt(jwt-> {
                        jwt.jwtAuthenticationConverter(jwtConverter());
                    });
                });

        return http.build();
    }

    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Optional.ofNullable(jwt.getClaimAsMap("realm_access"))
                    .map(m -> m.get("roles"))
                    .filter(List.class::isInstance)
                    .map(roles -> (List<?>) roles)
                    .ifPresent(roles -> roles.forEach(r ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + r))));

            return new JwtAuthenticationToken(jwt,authorities);
        };
    }
}

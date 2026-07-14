package com.rusobr.academic.config;

import jakarta.servlet.DispatcherType;
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

import static com.rusobr.academic.domain.enums.UserRole.*;
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
                            .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll().requestMatchers("/public/**", "/actuator/health").permitAll()
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers(GET, "/api/v1/academic-years").hasAnyRole(STUDENT.name(), TEACHER.name(), PARENT.name(), ADMIN.name())
                            .requestMatchers(GET, "/api/v1/academic-periods").hasAnyRole(STUDENT.name(), TEACHER.name(), PARENT.name(), ADMIN.name())
                            .requestMatchers(GET, "/api/v1/academic-periods/by-academic-year/*").hasAnyRole(STUDENT.name(), TEACHER.name(), ADMIN.name())

                            //STUDENT SCOPE
                            .requestMatchers(GET, "/api/v1/bff/students/home").hasRole(STUDENT.name())
                            .requestMatchers(GET, "/api/v1/schedules/diary").hasRole(STUDENT.name())
                            .requestMatchers(GET, "/api/v1/grades/by-student").hasRole(STUDENT.name())
                            .requestMatchers(GET, "/api/v1/final-grades/by-student").hasRole(STUDENT.name())
                            .requestMatchers(GET, "/api/v1/period-grades/by-student").hasRole(STUDENT.name())
                            .requestMatchers(GET, "/api/v1/school-classes/search/by-student").hasRole(STUDENT.name())

                            //TEACHER SCOPE
                            .requestMatchers(GET, "/api/v1/teaching-assignments").hasRole(TEACHER.name())
                            .requestMatchers(GET, "/api/v1/journal/by-assignment").hasRole(TEACHER.name())
                            .requestMatchers(GET, "/api/v1/period-grades/by-assignment").hasRole(TEACHER.name())
                            .requestMatchers(GET, "/api/v1/final-grades/by-assignment").hasRole(TEACHER.name())
                            .requestMatchers(POST, "/api/v1/attendances").hasRole(TEACHER.name())
                            .requestMatchers(POST, "/api/v1/grades").hasRole(TEACHER.name())
                            .requestMatchers(DELETE, "/api/v1/attendances/*").hasRole(TEACHER.name())
                            .requestMatchers(DELETE, "/api/v1/grades/*").hasRole(TEACHER.name())
                            .requestMatchers(POST, "/api/v1/period-grades").hasRole(TEACHER.name())
                            .requestMatchers(DELETE, "/api/v1/period-grades/*").hasRole(TEACHER.name())
                            .requestMatchers(POST, "/api/v1/final-grades").hasRole(TEACHER.name())
                            .requestMatchers(DELETE, "/api/v1/final-grades/*").hasRole(TEACHER.name())

                            .requestMatchers(GET, "/api/v1/homeworks/by-assignment").hasRole(TEACHER.name())
                            .requestMatchers(GET, "/api/v1/lesson-instances/by-assignment").hasRole(TEACHER.name())
                            .requestMatchers(POST, "/api/v1/homeworks").hasRole(TEACHER.name())
                            .requestMatchers(DELETE, "/api/v1/homeworks/*").hasRole(TEACHER.name())

                            //ADMIN SCOPE
                            .requestMatchers(GET, "/api/v1/subjects").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/subjects").hasRole(ADMIN.name())
                            .requestMatchers(DELETE, "/api/v1/subjects/*").hasRole(ADMIN.name())

                            .requestMatchers(POST, "/api/v1/academic-periods").hasRole(ADMIN.name())
                            .requestMatchers(PATCH, "/api/v1/academic-periods/*/close").hasRole(ADMIN.name())
                            .requestMatchers(PATCH, "/api/v1/academic-periods/*/open").hasRole(ADMIN.name())
                            .requestMatchers(PATCH, "/api/v1/academic-periods/*").hasRole(ADMIN.name())
                            .requestMatchers(DELETE, "/api/v1/academic-periods/*").hasRole(ADMIN.name())

                            .requestMatchers(GET, "/api/v1/school-classes/by-academic-year/*").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/school-classes").hasRole(ADMIN.name())
                            .requestMatchers(PATCH, "/api/v1/school-classes/*").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/school-classes/unassigned-students").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/school-classes/*/details").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/school-classes/*/students/*").hasRole(ADMIN.name())
                            .requestMatchers(DELETE, "/api/v1/school-classes/*/students/*").hasRole(ADMIN.name())
                            .requestMatchers(PUT, "/api/v1/school-classes/*/teacher/*").hasRole(ADMIN.name())

                            .requestMatchers(GET, "/api/v1/schedules").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/schedules").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/schedules/by-class").hasRole(ADMIN.name())
                            .requestMatchers(PATCH, "/api/v1/schedules/load").hasRole(ADMIN.name())
                            .requestMatchers(PATCH, "/api/v1/schedules/*/close").hasRole(ADMIN.name())
                            .requestMatchers(GET, "/api/v1/teacher-subjects").hasRole(ADMIN.name())

                            .requestMatchers(GET, "/api/v1/academic-years").hasRole(ADMIN.name())
                            .requestMatchers(POST, "/api/v1/academic-years").hasRole(ADMIN.name())
                            .requestMatchers(PATCH, "/api/v1/academic-years/*/open").hasRole(ADMIN.name())
                            .requestMatchers(PATCH, "/api/v1/academic-years/*/close").hasRole(ADMIN.name())
                            .requestMatchers(DELETE, "/api/v1/academic-years/*").hasRole(ADMIN.name())

                            .requestMatchers(POST, "/api/v1/teacher-subjects").hasRole(ADMIN.name())
                            .requestMatchers(DELETE, "/api/v1/teacher-subjects").hasRole(ADMIN.name())


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

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}

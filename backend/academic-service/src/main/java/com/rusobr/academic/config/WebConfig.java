package com.rusobr.academic.config;

import com.rusobr.academic.infrastructure.interceptor.StudentContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StudentContextInterceptor studentContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(studentContextInterceptor)
                .addPathPatterns("/api/v1/academic-years",
                        "/api/v1/academic-periods",
                        "/api/v1/teachers/*/info",
                        "/api/v1/bff/students/*/info",
                        "/api/v1/bff/students/home",
                        "/api/v1/schedules/diary",
                        "/api/v1/grades/by-student",
                        "/api/v1/grades/*/detail",
                        "/api/v1/period-final-grades/by-student",
                        "/api/v1/school-classes/by-student",
                        "/api/v1/pdf/student/grade-report/report",
                        "/api/v1/pdf/student/grade-period-report/report")
                .excludePathPatterns("/api/v1/students/by-parent");
    }
}

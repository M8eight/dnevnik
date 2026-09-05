package com.rusobr.user.config;

import com.rusobr.user.infrastructure.interceptor.StudentContextInterceptor;
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
                .addPathPatterns("/api/v1/students/with-class");
    }
}

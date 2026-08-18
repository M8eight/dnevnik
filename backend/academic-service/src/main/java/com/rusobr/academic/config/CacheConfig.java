package com.rusobr.academic.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(10))
                .maximumSize(500));

        cacheManager.registerCustomCache("academicYears",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(24))
                        .maximumSize(500)
                        .build());

        cacheManager.registerCustomCache("academicPeriods",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(6))
                        .maximumSize(500)
                        .build());

        cacheManager.registerCustomCache("teacherSubjects",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(6))
                        .maximumSize(500)
                        .build());

        cacheManager.registerCustomCache("attendanceStudentStatus",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(500)
                        .build());

        cacheManager.registerCustomCache("schoolClassByStudent",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(500)
                        .build());

        return cacheManager;
    }

}

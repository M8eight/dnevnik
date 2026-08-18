package com.rusobr.user.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(500));

        cacheManager.registerCustomCache("studentHomeInfo",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofHours(24))
                        .maximumSize(500)
                        .build());

        cacheManager.registerCustomCache("studentInfo",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(500)
                        .build());

        cacheManager.registerCustomCache("teacherWithUser",
                Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofMinutes(10))
                        .maximumSize(500)
                        .build());

        return cacheManager;
    }

}

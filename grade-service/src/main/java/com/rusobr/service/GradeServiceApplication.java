package com.rusobr.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GradeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GradeServiceApplication.class, args);
    }
}
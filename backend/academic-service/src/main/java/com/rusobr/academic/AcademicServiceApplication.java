package com.rusobr.academic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.rusobr.academic", "com.rusobr.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class AcademicServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcademicServiceApplication.class, args);
    }
}

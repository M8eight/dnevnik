package com.rusobr.user.feignIT;

import com.rusobr.user.infrastructure.client.feign.AcademicClient;
import com.rusobr.user.infrastructure.client.feign.AcademicClientFallbackFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableFeignClients(clients = AcademicClient.class)
@Import(AcademicClientFallbackFactory.class)
public class FeignITConfiguration {
}

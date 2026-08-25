package com.rusobr.academic.feignIT;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Tag("integration")
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "clients.user-service.url=http://localhost:${wiremock.server.port}",
        "resilience4j.circuitbreaker.instances.user-service.minimum-number-of-calls=1000000"
})
public abstract class FeignIntegrationTestBase {
}

package com.rusobr.academic.feignIT;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Tag("integration")
@Suite
@DisplayName("Feign tests")
@SelectClasses({
        FeignIntegrationTestBase.class,
        UserClientIT.class,
})
public class FeignIntegrationTestSuite {
}

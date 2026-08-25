package com.rusobr.user.feignIT;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Feign tests")
@SelectClasses({
        AcademicClientIT.class
})
public class FeignIntegrationTestSuite {
}

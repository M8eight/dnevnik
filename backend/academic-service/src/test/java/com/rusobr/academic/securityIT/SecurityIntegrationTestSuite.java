package com.rusobr.academic.securityIT;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Tag("integration")
@Suite
@DisplayName("Security tests")
@SelectClasses({
        OwnershipSecurityIT.class,
        ResourceServerSecurityIT.class,
        JwtTestUtils.class,
        KeycloakRoleConverterTest.class,
})
public class SecurityIntegrationTestSuite {
}

package com.rusobr.user.securityIT;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Security tests")
@SelectClasses({
        AbstractSecurityIT.class,
        OwnershipSecurityIT.class,
        ResourceServerSecurityIT.class,
        JwtTestUtils.class,
        KeycloakRoleConverterTest.class
})
public class SecurityIntegrationTestSuite {
}

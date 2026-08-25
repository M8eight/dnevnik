package com.rusobr.user.jpaIT;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Jpa tests")
@SelectClasses({
        ParentRepositoryIT.class,
        StudentRepositoryIT.class,
        TeacherRepositoryIT.class,
        UserRepositoryIT.class
})
public class JpaIntegrationTestSuite {
}

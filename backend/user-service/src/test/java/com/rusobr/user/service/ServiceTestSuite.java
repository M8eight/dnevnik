package com.rusobr.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Service tests")
@SelectClasses({
        ParentServiceTest.class,
        StudentServiceTest.class,
        TeacherServiceTest.class,
        UserDbServiceTest.class,
        UserOrchestratorTest.class,
        UserServiceTest.class
})
public class ServiceTestSuite {
}

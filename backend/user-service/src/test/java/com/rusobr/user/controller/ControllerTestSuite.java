package com.rusobr.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Controller tests")
@SelectClasses({
        ParentControllerTest.class,
        StudentControllerTest.class,
        TeacherControllerTest.class,
        UserControllerTest.class
})
public class ControllerTestSuite {
}

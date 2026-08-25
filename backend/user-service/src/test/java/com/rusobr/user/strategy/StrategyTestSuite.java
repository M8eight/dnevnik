package com.rusobr.user.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Strategy tests")
@SelectClasses({
        ParentStrategyTest.class,
        StudentStrategyTest.class,
        TeacherStrategyTest.class
})
public class StrategyTestSuite {
}

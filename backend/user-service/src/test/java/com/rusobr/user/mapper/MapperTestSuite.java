package com.rusobr.user.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Mapper tests")
@SelectClasses({
        ParentMapperTest.class,
        StudentMapperTest.class,
        TeacherMapperTest.class,
        UserMapperTest.class
})
public class MapperTestSuite {
}

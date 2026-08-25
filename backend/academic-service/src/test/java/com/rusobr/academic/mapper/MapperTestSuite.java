package com.rusobr.academic.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Mapper tests")
@SelectClasses({
        AcademicPeriodMapperTest.class,
        AcademicYearMapperTest.class,
        AttendanceMapperTest.class,
        ClassStudentMapperTest.class,
        FinalGradeMapperTest.class,
        GradeMapperTest.class,
        HomeworkMapperTest.class,
        LessonInstanceMapperTest.class,
        PeriodGradeMapperTest.class,
        ScheduleLessonMapperTest.class,
        SchoolClassMapperTest.class,
        SubjectMapperTest.class,
        TeacherSubjectMapperTest.class,
        TeachingAssignmentMapperTest.class
})
public class MapperTestSuite {

}

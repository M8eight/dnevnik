package com.rusobr.academic.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Controller tests")
@SelectClasses({
        AcademicPeriodControllerTest.class,
        AttendanceControllerTest.class,
        FinalGradeControllerTest.class,
        GradeControllerTest.class,
        HomeworkControllerTest.class,
        JournalControllerTest.class,
        PeriodGradeControllerTest.class,
        ScheduleControllerTest.class,
        SchoolClassControllerTest.class,
        SubjectControllerTest.class,
        TeacherSubjectControllerTest.class
})
public class ControllerTestSuite {
}

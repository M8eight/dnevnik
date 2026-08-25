package com.rusobr.academic.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@DisplayName("Service tests")
@SelectClasses({
        AcademicPeriodServiceTest.class,
        AcademicYearServiceTest.class,
        AttendanceServiceTest.class,
        ClassStudentServiceTest.class,
        FinalGradeServiceTest.class,
        GradeServiceTest.class,
        HomeworkServiceTest.class,
        JournalServiceTest.class,
        PeriodGradeServiceTest.class,
        ScheduleGeneratorServiceTest.class,
        ScheduleServiceTest.class,
        SchoolClassServiceTest.class,
        SubjectServiceTest.class,
        TeacherSubjectServiceTest.class,
        TeachingAssignmentServiceTest.class
})
public class ServiceTestSuite {

}

package com.rusobr.academic.jpaIT;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Tag("integration")
@Suite
@DisplayName("JPA tests")
@SelectClasses({
        AcademicPeriodRepositoryIT.class,
        AcademicYearRepositoryIT.class,
        AttendanceRepositoryIT.class,
        ClassStudentRepositoryIT.class,
        FinalGradeRepositoryIT.class,
        GradeRepositoryIT.class,
        HomeworkRepositoryIT.class,
        LessonInstanceRepositoryIT.class,
        PeriodGradeRepositoryIT.class,
        ScheduleLessonRepositoryIT.class,
        SchoolClassRepositoryIT.class,
        SubjectRepositoryIT.class,
        TeacherSubjectRepositoryIT.class,
        TeachingAssignmentRepositoryIT.class,
})
public class JpaIntegrationTestSuite {
}

package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.FinalGrade;
import com.rusobr.academic.domain.model.Grade;
import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeacherSubject;
import com.rusobr.academic.domain.model.TeacherSubjectId;
import com.rusobr.academic.domain.model.TeachingAssignment;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class TestData {

    public static final LocalDate SEPTEMBER = LocalDate.of(2025, 9, 1);

    private TestData() {
    }

    public static AcademicYear academicYear(String name) {
        return AcademicYear.builder()
                .name(name)
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 5, 31))
                .build();
    }

    public static AcademicPeriod academicPeriod(AcademicYear year, String name, LocalDate start, LocalDate end) {
        return AcademicPeriod.builder()
                .name(name)
                .academicYear(year)
                .startDate(start)
                .endDate(end)
                .build();
    }

    public static Subject subject(String name) {
        return Subject.builder()
                .name(name)
                .build();
    }

    public static SchoolClass schoolClass(AcademicYear year, String name, long classTeacherId) {
        return SchoolClass.builder()
                .name(name)
                .academicYear(year)
                .classTeacherId(classTeacherId)
                .build();
    }

    public static ClassStudent classStudent(SchoolClass schoolClass, long studentId) {
        ClassStudent classStudent = ClassStudent.builder()
                .studentId(studentId)
                .schoolClass(schoolClass)
                .build();
        schoolClass.getStudents().add(classStudent);
        return classStudent;
    }

    public static TeachingAssignment assignment(long teacherId, SchoolClass schoolClass, Subject subject) {
        return TeachingAssignment.builder()
                .teacherId(teacherId)
                .schoolClass(schoolClass)
                .subject(subject)
                .build();
    }

    public static ScheduleLesson scheduleLesson(TeachingAssignment assignment, DayOfWeek dayOfWeek, int lessonNumber) {
        return ScheduleLesson.builder()
                .teachingAssignment(assignment)
                .dayOfWeek(dayOfWeek)
                .lessonNumber(lessonNumber)
                .classRoom("101")
                .validFrom(SEPTEMBER)
                .build();
    }

    public static LessonInstance lessonInstance(ScheduleLesson scheduleLesson, LocalDate lessonDate) {
        return LessonInstance.builder()
                .scheduleLesson(scheduleLesson)
                .lessonDate(lessonDate)
                .build();
    }

    public static Grade grade(long studentId, LessonInstance lessonInstance, int value, int weight, GradeType type) {
        return Grade.builder()
                .studentId(studentId)
                .lessonInstance(lessonInstance)
                .value(value)
                .weight(weight)
                .type(type)
                .build();
    }

    public static Attendance attendance(long studentId, LessonInstance lessonInstance, AttendanceStatus status) {
        return Attendance.builder()
                .studentId(studentId)
                .lessonInstance(lessonInstance)
                .status(status)
                .build();
    }

    public static Homework homework(LessonInstance lessonInstance, String text) {
        return Homework.builder()
                .lessonInstance(lessonInstance)
                .text(text)
                .build();
    }

    public static PeriodGrade periodGrade(long studentId, AcademicPeriod period, TeachingAssignment assignment, int value) {
        return PeriodGrade.builder()
                .studentId(studentId)
                .academicPeriod(period)
                .teachingAssignment(assignment)
                .value(value)
                .build();
    }

    public static FinalGrade finalGrade(long studentId, AcademicYear year, TeachingAssignment assignment, int value) {
        return FinalGrade.builder()
                .studentId(studentId)
                .academicYear(year)
                .teachingAssignment(assignment)
                .value(value)
                .build();
    }

    public static TeacherSubject teacherSubject(long teacherId, Subject subject) {
        TeacherSubject teacherSubject = TeacherSubject.builder()
                .id(TeacherSubjectId.builder().teacherId(teacherId).subjectId(null).build())
                .subject(subject)
                .build();
        teacherSubject.getId().setSubjectId(subject.getId());
        return teacherSubject;
    }

}

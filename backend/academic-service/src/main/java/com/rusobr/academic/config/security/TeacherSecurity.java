package com.rusobr.academic.config.security;

import com.rusobr.academic.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("teacherSecurity")
@RequiredArgsConstructor
public class TeacherSecurity {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final LessonInstanceRepository lessonInstanceRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final HomeworkRepository homeworkRepository;
    private final PeriodGradeRepository periodGradeRepository;
    private final FinalGradeRepository finalGradeRepository;

    public boolean canViewAssignment(Long teachingAssignmentId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return teachingAssignmentRepository
                .isTeacherOwnedAssignment(teacherId, teachingAssignmentId);
    }

    //GRADE
    public boolean canCreateGrade(Long studentId, Long lessonInstanceId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return lessonInstanceRepository.isOwnedByTeacherAndHasStudent(teacherId, lessonInstanceId, studentId);
    }

    public boolean canDeleteGrade(Long gradeId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return gradeRepository.isGradeOwnedByTeacher(teacherId, gradeId);
    }

    //ATTENDANCE
    public boolean canCreateAttendance(Long studentId, Long lessonInstanceId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return lessonInstanceRepository.isOwnedByTeacherAndHasStudent(teacherId, lessonInstanceId, studentId);
    }

    public boolean canDeleteAttendance(Long attendanceId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return attendanceRepository.isAttendanceOwnedByTeacher(teacherId, attendanceId);
    }

    //HOMEWORK
    public boolean canCreateHomework(Long lessonInstanceId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return lessonInstanceRepository.isOwnedByTeacher(teacherId, lessonInstanceId);
    }

    public boolean canDeleteHomework(Long homeworkId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return homeworkRepository.isHomeworkOwnedByTeacher(teacherId, homeworkId);
    }

    //PERIOD-GRADE
    public boolean canCreatePeriodGrade(Long teachingAssignmentId, Long studentId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return teachingAssignmentRepository.isOwnedByTeacherWithStudent(teacherId, teachingAssignmentId, studentId);
    }

    public boolean canDeletePeriodGrade(Long periodGradeId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return periodGradeRepository.isPeriodGradeOwnedByTeacher(teacherId, periodGradeId);
    }

    //FINAL-GRADE
    public boolean canCreateFinalGrade(Long teachingAssignmentId, Long studentId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return teachingAssignmentRepository.isOwnedByTeacherWithStudent(teacherId, teachingAssignmentId, studentId);
    }

    public boolean canDeleteFinalGrade(Long finalGradeId, Authentication auth) {
        Long teacherId = currentTeacherId(auth);
        return finalGradeRepository.isFinalGradeOwnedByTeacher(teacherId, finalGradeId);
    }


    //helpers
    private Long currentTeacherId(Authentication auth) {
        Long teacherId = ((Jwt) auth.getPrincipal()).getClaim("user_id");
        if (teacherId == null) {
            throw new IllegalStateException("JWT claim 'user_id' is missing");
        }
        return teacherId;
    }

}

package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.Grade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance,Long> {

    Optional<Attendance> findByStudentIdAndLessonInstanceId(Long studentId, Long lessonInstanceId);

    @Query("""
        select count(*) > 0
        from Attendance a
            join a.lessonInstance li
            join li.scheduleLesson sl
            join sl.teachingAssignment ta
        where ta.teacherId = :teacherId
        and a.id = :gradeId
    """)
    boolean isAttendanceOwnedByTeacher(@Param("teacherId") Long teacherId, @Param("attendanceId") Long attendanceId);

    @EntityGraph(attributePaths = {"lessonInstance"})
    Optional<Attendance> findWithLessonInstanceById(Long id);

}

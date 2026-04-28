package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance,Long> {
    Optional<Attendance> findByStudentIdAndLessonInstanceId(Long studentId, Long lessonInstanceId);
}

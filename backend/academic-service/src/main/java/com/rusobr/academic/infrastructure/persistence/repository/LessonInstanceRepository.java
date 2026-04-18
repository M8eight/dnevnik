package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonInstanceRepository extends JpaRepository<LessonInstance, Long> {

    @Query("""
                select distinct li
                from LessonInstance li
                join fetch li.scheduleLesson sl
                join fetch sl.teachingAssignment ta
                join ta.schoolClass sc
                join sc.students st
                join fetch ta.subject su
                left join fetch Grade g on g.lessonInstance = li and g.studentId = :studentId
                left join fetch Attendance a on a.lessonInstance = li and a.studentId = :studentId
                left join fetch Homework h on h.lessonInstance = li
                where st.studentId = :studentId
                    and li.lessonDate between :startDate and :endDate
                    and (g.studentId = :studentId or g.id is null)
                    and (a.studentId = :studentId or a.id is null)
                order by li.lessonDate
            """)
    List<LessonInstance> findDiaryLessonsByStudentIdAndDateRange(@Param("studentId") Long studentId,
                                                                      @Param("startDate") LocalDate startDate,
                                                                      @Param("endDate") LocalDate endDate);

    Optional<LessonInstance> findByLessonDateAndScheduleLessonId(LocalDate date, Long scheduleLessonId);
}

package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.web.dto.lessonInstance.LessonWeekDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LessonInstanceRepository extends JpaRepository<LessonInstance, Long> {
    @Query("""
                select new com.rusobr.academic.web.dto.lessonInstance.LessonWeekDto(
                        li.date,
                        sl.lessonNumber,
                        sl.classRoom,
                        sbj.name,
                        g.value,
                        g.type,
                        a.status
                    )
                from LessonInstance li
                join li.scheduleLesson sl
                join sl.teachingAssignment ta
                join ta.subject sbj
                left join Grade g on g.lessonInstance = li and g.studentId = :student_id
                left join Attendance a on a.lessonInstance = li and a.studentId = :student_id
                where ta.schoolClass.id = :classId
                    and li.date between :startDate and :endDate
                order by li.date asc, sl.lessonNumber asc
            """)
    List<LessonWeekDto> getSchedule(
            @Param("classId") Long classId,
            @Param("student_id") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

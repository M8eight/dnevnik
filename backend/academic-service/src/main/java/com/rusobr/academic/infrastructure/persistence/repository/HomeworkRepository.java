package com.rusobr.academic.infrastructure.persistence.repository;

import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.web.dto.homework.HomeworkHomePageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HomeworkRepository extends CrudRepository<Homework, Long> {
    @Query("""
        select distinct new com.rusobr.academic.web.dto.homework.HomeworkHomePageResponse(
            h.id,
            h.text,
            su.name
        )
        from Homework h
        join h.lessonInstance li
        join li.scheduleLesson sl
        join sl.teachingAssignment ta
        join ta.subject su
        join ta.schoolClass sc
        join sc.students s
        where li.lessonDate = :date
        and s.studentId = :studentId
""")
    List<HomeworkHomePageResponse> findHomeworksByDate(@Param("date") LocalDate date, @Param("studentId") Long studentId);

    @Query("""
    select h
    from Homework h
    join h.lessonInstance li
    join li.scheduleLesson sl
    join sl.teachingAssignment ta
    where ta.id = :teachingAssignmentId
    order by li.lessonDate asc
""")
    Page<Homework> findHomeworksByTeachingAssignmentId(@Param("teachingAssignmentId") Long teachingAssignmentId,
                                                               Pageable pageable);
}

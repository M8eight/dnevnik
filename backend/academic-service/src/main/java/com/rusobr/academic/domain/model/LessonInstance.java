package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "scheduleLesson")
@Builder
@Table(name = "lesson_instances")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update lesson_instances set deleted_at = now() where id = ?")
public class LessonInstance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_lesson_id", nullable = false)
    private ScheduleLesson scheduleLesson;

    @Column(name = "lesson_date", nullable = false)
    private LocalDate date;
}

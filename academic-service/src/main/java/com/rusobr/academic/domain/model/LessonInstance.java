package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "lesson_instances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"schedule_lesson_id", "lesson_date"}
        ))
public class LessonInstance {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_lesson_id", nullable = false)
    private ScheduleLesson scheduleLesson;

    @Column(name = "lesson_date", nullable = false)
    private LocalDate date;
}

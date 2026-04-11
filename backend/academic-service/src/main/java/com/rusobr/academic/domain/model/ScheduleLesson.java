package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "teachingAssignment")
@Builder
@Table(name = "schedule_lessons")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update schedule_lessons set deleted_at = now() where id = ?")
public class ScheduleLesson extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "lesson_number", nullable = false)
    private Integer lessonNumber;

    private String classRoom;

    private LocalDate validFrom;

    private LocalDate validTo;

}

package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Lesson {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teaching_assignment_id")
    private TeachingAssignment teachingAssignment;

    @OneToMany(mappedBy = "lesson")
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "lesson")
    private List<Grade> grades;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private Integer lessonNumber;

    private String classRoom;
}

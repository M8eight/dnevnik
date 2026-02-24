package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "teaching_assignments", uniqueConstraints = @UniqueConstraint(columnNames = {
        "teacher_id", "school_class_id", "subject_id"
}))
public class TeachingAssignment {
    @Id
    @GeneratedValue
    private Long id;

    private Long teacherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}

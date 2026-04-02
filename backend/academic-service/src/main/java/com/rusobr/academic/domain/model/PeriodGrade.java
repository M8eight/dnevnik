package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"academicPeriod", "teachingAssignment"})
@EntityListeners(AuditingEntityListener.class)
@Table(name = "period_grades", uniqueConstraints = @UniqueConstraint(columnNames = {
        "student_id", "academic_period_id", "teaching_assignment_id"
}))
public class PeriodGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Max(5)
    @Min(1)
    @Column(nullable = false)
    private int value;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id",  nullable = false)
    private AcademicPeriod academicPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teaching_assignment_id",  nullable = false)
    private TeachingAssignment teachingAssignment;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @CreatedDate
    @Column(updatable = false, name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

}

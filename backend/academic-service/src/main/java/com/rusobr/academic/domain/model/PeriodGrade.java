package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"academicPeriod", "teachingAssignment"})
@Table(name = "period_grades")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update period_grades set deleted_at = now() where id = ?")
public class PeriodGrade extends BaseEntity {
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
}

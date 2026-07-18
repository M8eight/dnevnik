package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import com.rusobr.common.entity.BaseEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(exclude = {"teachingAssignment", "academicYear"})
@Table(name = "final_grades")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update final_grades set deleted_at = now() where id = ?")
public class FinalGrade extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id",  nullable = false)
    private AcademicYear academicYear;

    @Max(5)
    @Min(1)
    @Column(nullable = false)
    private Integer value;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teaching_assignment_id",  nullable = false)
    private TeachingAssignment teachingAssignment;

}

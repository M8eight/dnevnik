package com.rusobr.academic.domain.model;

import com.rusobr.academic.domain.enums.GradeType;
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
@ToString(exclude = "lessonInstance")
@Builder
@Table(name = "grades")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update grades set deleted_at = now() where id = ?")
public class Grade extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_instance_id", nullable = false)
    private LessonInstance lessonInstance;

    @Max(5)
    @Min(1)
    private Integer value;

    @Min(1)
    private Integer weight;

    //TODO сделать enum
    @Enumerated(EnumType.STRING)
    private GradeType type;

}
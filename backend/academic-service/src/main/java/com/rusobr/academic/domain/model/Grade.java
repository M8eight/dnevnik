package com.rusobr.academic.domain.model;

import com.rusobr.academic.domain.enums.GradeType;
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
@ToString(exclude = "lessonInstance")
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "grades")
public class Grade {
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

    @CreatedDate
    @Column(updatable = false, name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

}
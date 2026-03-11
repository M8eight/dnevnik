package com.rusobr.academic.domain.model;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "attendances")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private LessonInstance lessonInstance;

    @CreatedDate
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

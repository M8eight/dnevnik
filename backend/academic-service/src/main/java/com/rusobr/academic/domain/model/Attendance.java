package com.rusobr.academic.domain.model;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import jakarta.persistence.*;
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
@Table(name = "attendances")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update attendances set deleted_at = now() where id = ?")
public class Attendance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private LessonInstance lessonInstance;
}

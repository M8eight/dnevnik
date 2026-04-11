package com.rusobr.academic.domain.model;

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
@Table(name = "homeworks")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update homeworks set deleted_at = now() where id = ?")
public class Homework extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_instance_id")
    private LessonInstance lessonInstance;

    //todo добавление файлов сделать

}

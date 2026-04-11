package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "schoolClass")
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "class_students")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update class_students set deleted_at = now() where id = ?")
public class ClassStudent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_class_id")
    private SchoolClass schoolClass;

}

package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "class_students")
@Getter
@Setter
@ToString(exclude = "schoolClass")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ClassStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,  nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_class_id")
    private SchoolClass schoolClass;

}

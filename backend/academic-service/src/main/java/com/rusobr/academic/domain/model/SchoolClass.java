package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;
import com.rusobr.common.entity.BaseEntity;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "school_classes")
@Getter
@Setter
@ToString(exclude = {"students", "academicYear"})
@Builder
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update school_classes set deleted_at = now() where id = ?")
public class SchoolClass extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id",  nullable = false)
    private AcademicYear academicYear;

    private Long classTeacherId;

    @Builder.Default
    @OneToMany(mappedBy = "schoolClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassStudent> students = new HashSet<>();
}

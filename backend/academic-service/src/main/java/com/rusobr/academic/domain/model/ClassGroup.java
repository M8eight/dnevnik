package com.rusobr.academic.domain.model;

import com.rusobr.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "classGroupStudents, schoolClass")
@Builder
@Table(name = "class_group")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update class_group set deleted_at = now() where id = ?")
public class ClassGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id", nullable = false)
    private SchoolClass schoolClass;

    @Builder.Default
    @OneToMany(mappedBy = "classGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassGroupStudents> classGroupStudents = new HashSet<>();

}

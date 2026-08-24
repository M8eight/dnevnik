package com.rusobr.academic.domain.model;

import com.rusobr.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "classGroup")
@Builder
@Table(name = "class_group_students")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update class_group_students set deleted_at = now() where id = ?")
public class ClassGroupStudents extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_group_id")
    private ClassGroup classGroup;

}

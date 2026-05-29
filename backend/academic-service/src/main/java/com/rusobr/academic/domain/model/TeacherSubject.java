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
@ToString
@Builder
@Table(name = "teacher_subjects")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update teacher_subjects set deleted_at = now() where teacher_id = ? and subject_id = ?")
public class TeacherSubject extends BaseEntity {

    @EmbeddedId
    private TeacherSubjectId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subjectId")
    @JoinColumn(name = "subject_id")
    private Subject subject;

}

package com.rusobr.academic.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TeacherSubjectId implements Serializable {

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "subject_id")
    private Long subjectId;

}

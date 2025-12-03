package com.rusobr.service.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Grade {
    @Id
    @GeneratedValue
    private Long id;

    String student;

    String subject;

    String teacher;

    //TODO сделать связь с константами оценок
//    @ManyToOne(optional = false, fetch = FetchType.EAGER)
//    GradeConstant gradeConstant;

    Date date;

    @CreationTimestamp
    @Column
    private java.sql.Timestamp created_at;

    @UpdateTimestamp
    @Column
    private java.sql.Timestamp updated_at;

}

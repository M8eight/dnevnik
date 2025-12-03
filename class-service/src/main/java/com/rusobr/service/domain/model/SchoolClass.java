package com.rusobr.service.domain.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class SchoolClass {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    //TODO связь с учениками

    //TODO учитель привязать

    //TODO расписание

    @CreationTimestamp
    @Column
    private java.sql.Timestamp created_at;

    @UpdateTimestamp
    @Column
    private java.sql.Timestamp updated_at;
}

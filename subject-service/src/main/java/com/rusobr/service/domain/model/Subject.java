package com.rusobr.service.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class Subject {
    @Id
    @GeneratedValue
    private Long id;

    String name;

    String teacher;

    @CreationTimestamp
    @Column
    private java.sql.Timestamp created_at;

    @UpdateTimestamp
    @Column
    private java.sql.Timestamp updated_at;

}

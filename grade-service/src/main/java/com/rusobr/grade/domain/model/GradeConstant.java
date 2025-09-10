package com.rusobr.grade.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class GradeConstant {
    @Id
    @GeneratedValue
    private Long id;

    String name;

    String description;

    Integer value;
}

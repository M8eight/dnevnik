package com.rusobr.service.domain.model;

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
public class Student {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    //TODO add class_id

    private String contact;


}

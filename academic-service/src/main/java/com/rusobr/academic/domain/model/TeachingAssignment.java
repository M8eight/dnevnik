package com.rusobr.academic.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class TeachingAssignment {
    @Id
    @GeneratedValue
    private Long id;

    private Long teacherId;

    @ManyToOne
    private SchoolClass schoolClass;

    @ManyToOne
    private Subject subject;
}

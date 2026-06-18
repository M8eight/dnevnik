package com.rusobr.academic.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Table(name = "academic_years")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update academic_years set deleted_at = now() where id = ?")
public class AcademicYear extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    private String description;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    @PreUpdate
    public void normalize() {
        normalizeAndValidateDates();
        normalizeName();
    }

    private void normalizeAndValidateDates() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    private void normalizeName() {
        if (name == null || name.isBlank()) {
            if (startDate != null && endDate != null) {
                name = String.format("%d-%d", startDate.getYear(), endDate.getYear());
            }
        } else {
            name = name.trim();
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}

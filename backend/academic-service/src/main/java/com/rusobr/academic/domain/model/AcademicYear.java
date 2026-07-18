package com.rusobr.academic.domain.model;

import com.rusobr.common.exception.ConflictException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import com.rusobr.common.entity.BaseEntity;

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
    @Setter(AccessLevel.NONE)
    private boolean closed = false;

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public void close() {
        if (this.closed) {
            throw new ConflictException("Academic year is already closed", AcademicExceptionCode.ACADEMIC_YEAR_CLOSE_CONFLICT);
        }
        this.closed = true;
    }

    public void open() {
        if (!this.closed) {
            throw new ConflictException("Academic year is already open", AcademicExceptionCode.ACADEMIC_YEAR_OPEN_CONFLICT);
        }
        this.closed = false;
    }
}

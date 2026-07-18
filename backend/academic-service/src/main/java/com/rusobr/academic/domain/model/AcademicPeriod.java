package com.rusobr.academic.domain.model;

import com.rusobr.common.exception.ConflictException;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import jakarta.persistence.*;
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
@ToString(exclude = {"academicYear"})
@Builder
@Table(name = "academic_periods")
@SQLRestriction("deleted_at is NULL")
@SQLDelete(sql = "update academic_periods set deleted_at = now() where id = ?")
public class AcademicPeriod extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Builder.Default
    @Setter(AccessLevel.NONE)
    private boolean closed = false;

    private LocalDate startDate;

    private LocalDate endDate;

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public void close() {
        if (this.closed) {
            throw new ConflictException("Academic period is already open", AcademicExceptionCode.ACADEMIC_PERIOD_CLOSE_CONFLICT);
        }
        this.closed = true;
    }

    public void open() {
        if (!this.closed) {
            throw new ConflictException("Academic period is already closed", AcademicExceptionCode.ACADEMIC_PERIOD_OPEN_CONFLICT);
        }
        this.closed = false;
    }

}

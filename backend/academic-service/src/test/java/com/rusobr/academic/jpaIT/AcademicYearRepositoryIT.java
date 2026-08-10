package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AcademicYearRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    AcademicYearRepository academicYearRepository;

    @Nested
    @DisplayName("findAllByOrderByStartDateDesc")
    class FindAllByOrderByStartDateDesc {

        @Test
        @DisplayName("возвращает учебные годы от новых к старым")
        void success() {
            AcademicYear older = persist(AcademicYear.builder()
                    .name("2024-2025")
                    .startDate(LocalDate.of(2024, 9, 1))
                    .endDate(LocalDate.of(2025, 5, 31))
                    .build());
            persist(AcademicYear.builder()
                    .name("2025-2026")
                    .startDate(LocalDate.of(2025, 9, 1))
                    .endDate(LocalDate.of(2026, 5, 31))
                    .build());

            List<AcademicYear> years = academicYearRepository.findAllByOrderByStartDateDesc();

            assertThat(years).extracting(AcademicYear::getName)
                    .containsExactly("2025-2026", "2024-2025");
            assertThat(years.get(1).getId()).isEqualTo(older.getId());
        }
    }

}

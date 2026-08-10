package com.rusobr.academic.jpaIT;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AcademicPeriodRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    AcademicPeriodRepository academicPeriodRepository;

    @Nested
    @DisplayName("findByDate")
    class FindByDate {

        @Test
        @DisplayName("возвращает период, содержащий дату")
        void success() {
            AcademicYear year = persist(TestData.academicYear("2025-2026"));
            persist(TestData.academicPeriod(year, "1 четверть",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 31)));

            Optional<AcademicPeriod> period =
                    academicPeriodRepository.findByDate(LocalDate.of(2025, 10, 15));

            assertThat(period).isPresent();
            assertThat(period.get().getName()).isEqualTo("1 четверть");
        }

        @Test
        @DisplayName("возвращает пусто, если дата вне всех периодов")
        void returnsEmptyForDateOutsideAllPeriods() {
            AcademicYear year = persist(TestData.academicYear("2025-2026"));
            persist(TestData.academicPeriod(year, "1 четверть",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 31)));

            Optional<AcademicPeriod> period =
                    academicPeriodRepository.findByDate(LocalDate.of(2025, 8, 1));

            assertThat(period).isNotPresent();
        }
    }

    @Nested
    @DisplayName("findAllOrderAsc")
    class FindAllOrderAsc {

        @Test
        @DisplayName("загружает учебный год и сортирует периоды по дате начала")
        void success() {
            AcademicYear year = persist(TestData.academicYear("2025-2026"));
            persist(TestData.academicPeriod(year, "2 четверть",
                    LocalDate.of(2025, 11, 10), LocalDate.of(2025, 12, 26)));
            persist(TestData.academicPeriod(year, "1 четверть",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 31)));

            List<AcademicPeriod> periods = academicPeriodRepository.findAllOrderAsc();

            assertThat(periods)
                    .extracting(AcademicPeriod::getName)
                    .containsExactly("1 четверть", "2 четверть");
            assertThat(periods).allSatisfy(p -> assertThat(p.getAcademicYear()).isNotNull());
        }
    }

    @Nested
    @DisplayName("findWithAcademicYearById")
    class FindWithAcademicYearById {

        @Test
        @DisplayName("загружает учебный год периода")
        void success() {
            AcademicYear year = persist(TestData.academicYear("2025-2026"));
            AcademicPeriod period = persist(TestData.academicPeriod(year, "1 четверть",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 31)));
            clearAndFlush();

            Optional<AcademicPeriod> found = academicPeriodRepository.findWithAcademicYearById(period.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getAcademicYear().getName()).isEqualTo("2025-2026");
        }
    }

    @Nested
    @DisplayName("findAllByAcademicYearIdOrderByStartDateAsc")
    class FindAllByAcademicYearIdOrderByStartDateAsc {

        @Test
        @DisplayName("возвращает только периоды указанного года в порядке возрастания")
        void success() {
            AcademicYear year = persist(TestData.academicYear("2025-2026"));
            AcademicYear other = persist(AcademicYear.builder()
                    .name("2024-2025")
                    .startDate(LocalDate.of(2024, 9, 1))
                    .endDate(LocalDate.of(2025, 5, 31))
                    .build());
            persist(TestData.academicPeriod(year, "2 четверть",
                    LocalDate.of(2025, 11, 10), LocalDate.of(2025, 12, 26)));
            persist(TestData.academicPeriod(year, "1 четверть",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 31)));
            persist(TestData.academicPeriod(other, "1 четверть",
                    LocalDate.of(2024, 9, 1), LocalDate.of(2024, 10, 31)));

            List<AcademicPeriod> periods = academicPeriodRepository
                    .findAllByAcademicYearIdOrderByStartDateAsc(year.getId());

            assertThat(periods).extracting(AcademicPeriod::getName)
                    .containsExactly("1 четверть", "2 четверть");
        }
    }

    @Nested
    @DisplayName("getAcademicPeriodsByAcademicYearId")
    class GetAcademicPeriodsByAcademicYearId {

        @Test
        @DisplayName("возвращает периоды учебного года")
        void success() {
            AcademicYear year = persist(TestData.academicYear("2025-2026"));
            persist(TestData.academicPeriod(year, "1 четверть",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 31)));

            List<AcademicPeriod> periods = academicPeriodRepository
                    .getAcademicPeriodsByAcademicYearId(year.getId());

            assertThat(periods).hasSize(1);
            assertThat(periods.get(0).getName()).isEqualTo("1 четверть");
        }
    }

    @Nested
    @DisplayName("existsByName")
    class ExistsByName {

        @Test
        @DisplayName("проверяет наличие периода с указанным именем")
        void success() {
            AcademicYear year = persist(TestData.academicYear("2025-2026"));
            persist(TestData.academicPeriod(year, "1 четверть",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 31)));

            assertThat(academicPeriodRepository.existsByName("1 четверть")).isTrue();
            assertThat(academicPeriodRepository.existsByName("3 четверть")).isFalse();
        }
    }

}

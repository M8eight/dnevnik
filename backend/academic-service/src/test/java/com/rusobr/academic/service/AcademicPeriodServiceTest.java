package com.rusobr.academic.service;

import com.rusobr.academic.domain.model.AcademicPeriod;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.AcademicPeriodMapper;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicPeriodRepository;
import com.rusobr.academic.infrastructure.service.AcademicPeriodService;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodRequest;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AcademicPeriodServiceTest {

    @Mock AcademicPeriodRepository academicPeriodRepository;
    @Mock AcademicPeriodMapper academicPeriodMapper;

    @InjectMocks AcademicPeriodService service;

    // ─────────────────────────────────────────────────────────────
    // getAcademicPeriods
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAcademicPeriods")
    class GetAcademicPeriods {

        @Test
        @DisplayName("возвращает список DTO напрямую из репозитория")
        void returnsMappedList() {
            AcademicPeriodResponse dto = new AcademicPeriodResponse(
                    1L, "Q1", "2025-2026", false,
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));

            when(academicPeriodRepository.findAllOrderAsc()).thenReturn(List.of(dto));

            List<AcademicPeriodResponse> result = service.getAcademicPeriods();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(dto);
            verifyNoInteractions(academicPeriodMapper); // маппер не используется
        }

        @Test
        @DisplayName("нет периодов — возвращает пустой список")
        void emptyRepository_returnsEmpty() {
            when(academicPeriodRepository.findAllOrderAsc()).thenReturn(List.of());

            List<AcademicPeriodResponse> result = service.getAcademicPeriods();

            assertThat(result).isEmpty();
            verifyNoInteractions(academicPeriodMapper);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // createAcademicPeriod
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createAcademicPeriod")
    class CreateAcademicPeriod {

        @Test
        @DisplayName("успешно создаёт и возвращает DTO")
        void success() {
            AcademicPeriodRequest req = new AcademicPeriodRequest(
                    "Q1", "2025-2026",
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));

            AcademicPeriod entity = AcademicPeriod.builder()
                    .name("Q1").schoolYear("2025-2026")
                    .startDate(LocalDate.of(2025, 9, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .build();

            AcademicPeriodResponse dto = new AcademicPeriodResponse(
                    1L, "Q1", "2025-2026", false,
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));

            when(academicPeriodMapper.toEntity(req)).thenReturn(entity);
            when(academicPeriodRepository.save(entity)).thenReturn(entity);
            when(academicPeriodMapper.toDto(entity)).thenReturn(dto);

            AcademicPeriodResponse result = service.createAcademicPeriod(req);

            assertThat(result).isEqualTo(dto);
            verify(academicPeriodRepository).save(entity);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // closePeriod
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("closePeriod")
    class ClosePeriod {

        @Test
        @DisplayName("успешно закрывает период")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().closed(false).build();

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

            service.closePeriod(1L);

            assertThat(period.isClosed()).isTrue();
        }

        @Test
        @DisplayName("период не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.closePeriod(1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic period with id 1 not found");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // openPeriod
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("openPeriod")
    class OpenPeriod {

        @Test
        @DisplayName("успешно открывает период")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().closed(true).build();

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

            service.openPeriod(1L);

            assertThat(period.isClosed()).isFalse();
        }

        @Test
        @DisplayName("период не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.openPeriod(1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic period with id 1 not found");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // setDateById
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("setDateById")
    class SetDateById {

        @Test
        @DisplayName("успешно обновляет все поля")
        void success_updatesAllFields() {
            AcademicPeriod period = AcademicPeriod.builder()
                    .closed(false)
                    .startDate(LocalDate.of(2025, 9, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .build();
            AcademicPeriodResponse req = new AcademicPeriodResponse(
                    null, "Q1", "2025-2026", false,
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

            service.setDateById(1L, req);

            assertThat(period.getName()).isEqualTo("Q1");
            assertThat(period.getSchoolYear()).isEqualTo("2025-2026");
        }

        @Test
        @DisplayName("период не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            AcademicPeriodResponse req = new AcademicPeriodResponse(
                    null, null, null, false, null, null);

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.setDateById(1L, req))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic period with id 1 not found");
        }

        @Test
        @DisplayName("период закрыт — бросает ConflictException")
        void periodClosed_throwsConflictException() {
            AcademicPeriod period = AcademicPeriod.builder().closed(true).build();
            AcademicPeriodResponse req = new AcademicPeriodResponse(
                    null, null, null, false, null, null);

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

            assertThatThrownBy(() -> service.setDateById(1L, req))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic period is closed");
        }

        @Test
        @DisplayName("startDate после endDate — бросает ConflictException")
        void startAfterEnd_throwsConflictException() {
            AcademicPeriod period = AcademicPeriod.builder()
                    .closed(false)
                    .startDate(LocalDate.of(2025, 9, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .build();
            // новая startDate позже существующей endDate
            AcademicPeriodResponse req = new AcademicPeriodResponse(
                    null, null, null, false,
                    LocalDate.of(2025, 12, 1), null);

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

            assertThatThrownBy(() -> service.setDateById(1L, req))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Final start date cannot be after end date");
        }

        @Test
        @DisplayName("null поля в запросе — не затирает существующие значения")
        void nullFields_preservesExistingValues() {
            AcademicPeriod period = AcademicPeriod.builder()
                    .closed(false)
                    .name("Old name")
                    .schoolYear("2024-2025")
                    .startDate(LocalDate.of(2025, 9, 1))
                    .endDate(LocalDate.of(2025, 11, 30))
                    .build();
            AcademicPeriodResponse req = new AcademicPeriodResponse(
                    null, null, null, false, null, null);

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

            service.setDateById(1L, req);

            assertThat(period.getName()).isEqualTo("Old name");
            assertThat(period.getSchoolYear()).isEqualTo("2024-2025");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // findById
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("возвращает маппированный DTO")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().build();
            AcademicPeriodResponse dto = new AcademicPeriodResponse(
                    1L, "Q1", "2025-2026", false,
                    LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
            when(academicPeriodMapper.toDto(period)).thenReturn(dto);

            AcademicPeriodResponse result = service.findById(1L);

            assertThat(result).isEqualTo(dto);
        }

        @Test
        @DisplayName("период не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic period with id 1 not found"); // покрывает "...not found!"
        }
    }

    // ─────────────────────────────────────────────────────────────
    // deleteById
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("успешно удаляет период")
        void success() {
            AcademicPeriod period = AcademicPeriod.builder().build();

            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

            service.deleteById(1L);

            verify(academicPeriodRepository).delete(period);
        }

        @Test
        @DisplayName("период не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            when(academicPeriodRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteById(1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic Period not found");

            verify(academicPeriodRepository, never()).delete(any());
        }
    }
}
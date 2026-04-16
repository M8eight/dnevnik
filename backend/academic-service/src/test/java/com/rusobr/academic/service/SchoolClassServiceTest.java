package com.rusobr.academic.service;

import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.mapper.SchoolClassMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.service.SchoolClassService;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchoolClassServiceTest {

    @Mock SchoolClassRepository schoolClassRepository;
    @Mock SchoolClassMapper schoolClassMapper;

    @InjectMocks SchoolClassService service;

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("возвращает маппированный DTO")
        void success() {
            SchoolClass schoolClass = SchoolClass.builder().id(1L).name("10А").year("2025-2026").classTeacherId(5L).build();
            SchoolClassResponse expected = new SchoolClassResponse(1L, "10А", "2025-2026", 5L);

            when(schoolClassRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
            when(schoolClassMapper.toSchoolClassResponse(schoolClass)).thenReturn(expected);

            SchoolClassResponse result = service.findById(1L);

            assertThat(result).isEqualTo(expected);
            verify(schoolClassMapper).toSchoolClassResponse(schoolClass);
        }

        @Test
        @DisplayName("класс не найден — бросает NotFoundException")
        void notFound_throwsNotFoundException() {
            when(schoolClassRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass Not Found by id: 99");

            verifyNoInteractions(schoolClassMapper);
        }
    }

    @Nested
    @DisplayName("findClassByStudentId")
    class FindClassByStudentId {

        @Test
        @DisplayName("возвращает DTO если студент найден")
        void success() {
            SchoolClassResponse expected = new SchoolClassResponse(1L, "10А", "2025-2026", 5L);

            when(schoolClassRepository.getSchoolClassByStudentId(1L)).thenReturn(Optional.of(expected));

            SchoolClassResponse result = service.findClassByStudentId(1L);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("студент не привязан к классу — возвращает null")
        void studentNotInClass_returnsNull() {
            when(schoolClassRepository.getSchoolClassByStudentId(99L)).thenReturn(Optional.empty());

            SchoolClassResponse result = service.findClassByStudentId(99L);

            assertThat(result).isNull();
        }
    }
}
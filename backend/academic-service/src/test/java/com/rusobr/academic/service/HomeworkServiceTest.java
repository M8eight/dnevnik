package com.rusobr.academic.service;

import com.rusobr.academic.infrastructure.persistence.repository.HomeworkRepository;
import com.rusobr.academic.infrastructure.service.HomeworkService;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeworkServiceTest {

    @Mock HomeworkRepository homeworkRepository;

    @InjectMocks HomeworkService service;

    @Nested
    @DisplayName("getHomeworksByDate")
    class GetHomeworksByDate {

        @Test
        @DisplayName("возвращает список домашних заданий для студента на дату")
        void success() {
            LocalDate date = LocalDate.of(2026, 4, 14);
            HomeworkResponse hw = new HomeworkResponse(1L, "Стр. 42, упр. 5", "Математика");

            when(homeworkRepository.findHomeworksByDate(date, 1L)).thenReturn(List.of(hw));

            List<HomeworkResponse> result = service.getHomeworksByDate(date, 1L);

            assertThat(result).containsExactly(hw);
            verify(homeworkRepository).findHomeworksByDate(date, 1L);
        }

        @Test
        @DisplayName("нет заданий на дату — возвращает пустой список")
        void noHomeworks_returnsEmpty() {
            LocalDate date = LocalDate.of(2026, 4, 14);

            when(homeworkRepository.findHomeworksByDate(date, 1L)).thenReturn(List.of());

            List<HomeworkResponse> result = service.getHomeworksByDate(date, 1L);

            assertThat(result).isEmpty();
        }
    }
}
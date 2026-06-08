package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.TeachingAssignmentMapper;
import com.rusobr.academic.application.service.TeachingAssignmentService;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.projection.TeachingAssignmentDetailsProjection;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentDetailsDto;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeachingAssignmentServiceTest {

    @Mock private TeachingAssignmentRepository teachingAssignmentRepository;
    @Mock private SchoolClassRepository schoolClassRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private TeachingAssignmentMapper teachingAssignmentMapper;

    @InjectMocks private TeachingAssignmentService service;

    private static final Long CLASS_ID = 1L;
    private static final Long SUBJECT_ID = 2L;
    private static final Long TEACHER_ID = 3L;

    @Nested
    @DisplayName("createOrGet")
    class CreateOrGet {
        @Test
        @DisplayName("возвращает существующее назначение, если оно есть")
        void returnExisting() {
            TeachingAssignmentRequest request = new TeachingAssignmentRequest(CLASS_ID, SUBJECT_ID, TEACHER_ID);
            TeachingAssignment existing = TeachingAssignment.builder().id(100L).build();

            when(teachingAssignmentRepository.findBySubjectIdAndSchoolClassIdAndTeacherId(SUBJECT_ID, CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.of(existing));

            TeachingAssignment result = service.createOrGet(request);

            assertThat(result).isEqualTo(existing);
            verifyNoInteractions(schoolClassRepository, subjectRepository);
        }

        @Test
        @DisplayName("создает новое назначение, если его нет")
        void createNew() {
            TeachingAssignmentRequest request = new TeachingAssignmentRequest(CLASS_ID, SUBJECT_ID, TEACHER_ID);
            SchoolClass schoolClass = new SchoolClass();
            Subject subject = new Subject();

            when(teachingAssignmentRepository.findBySubjectIdAndSchoolClassIdAndTeacherId(SUBJECT_ID, CLASS_ID, TEACHER_ID))
                    .thenReturn(Optional.empty());
            when(schoolClassRepository.getReferenceById(CLASS_ID)).thenReturn(schoolClass);
            when(subjectRepository.getReferenceById(SUBJECT_ID)).thenReturn(subject);
            when(teachingAssignmentRepository.save(any(TeachingAssignment.class))).thenAnswer(i -> i.getArgument(0));

            TeachingAssignment result = service.createOrGet(request);

            assertThat(result.getSchoolClass()).isEqualTo(schoolClass);
            assertThat(result.getSubject()).isEqualTo(subject);
            verify(teachingAssignmentRepository).save(any(TeachingAssignment.class));
        }
    }

    @Nested
    @DisplayName("getStudentIdsByTeachingAssignmentId")
    class GetStudentIds {
        @Test
        @DisplayName("возвращает список ID студентов")
        void success() {
            Long assignmentId = 55L;
            List<Long> ids = List.of(1L, 2L, 3L);

            when(teachingAssignmentRepository.findStudentIdsByTeachingAssignmentId(assignmentId))
                    .thenReturn(ids);

            List<Long> result = service.getStudentIdsByTeachingAssignmentId(assignmentId);

            assertThat(result).hasSize(3).isEqualTo(ids);
        }
    }

    @Nested
    @DisplayName("getByTeacherId")
    class GetByTeacherId {
        @Test
        @DisplayName("возвращает детали назначений учителя")
        void success() {
            TeachingAssignmentDetailsProjection projection = mock(TeachingAssignmentDetailsProjection.class);
            TeachingAssignmentDetailsDto dto = new TeachingAssignmentDetailsDto(1L, CLASS_ID, "10A", SUBJECT_ID, "Математика");

            when(teachingAssignmentRepository.findTeachingAssignmentDetailByTeacherId(TEACHER_ID))
                    .thenReturn(List.of(projection));
            when(teachingAssignmentMapper.toTeachingAssignmentDetailsDto(projection))
                    .thenReturn(dto);

            List<TeachingAssignmentDetailsDto> result = service.getByTeacherId(TEACHER_ID);

            assertThat(result).hasSize(1).containsExactly(dto);
        }
    }
}
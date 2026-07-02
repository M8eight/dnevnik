package com.rusobr.academic.service;

import com.rusobr.academic.application.mapper.SchoolClassMapper;
import com.rusobr.academic.application.service.SchoolClassService;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.feign.TeacherDetails;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassUpdateRequest;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceTest {

    @Mock
    private SchoolClassRepository schoolClassRepository;
    @Mock
    private SchoolClassMapper schoolClassMapper;
    @Mock
    private UserClient userClient;
    @Mock
    private AcademicYearRepository academicYearRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "self", service);
    }

    @InjectMocks
    private SchoolClassService service;

    private static final Long CLASS_ID = 1L;
    private static final Long STUDENT_ID = 42L;
    private static final Long TEACHER_ID = 7L;
    private static final Long ACADEMIC_YEAR_ID = 100L;
    private static final String CLASS_NAME = "10A";
    private static final String UPDATED_CLASS_NAME = "10B";

    private AcademicYear buildAcademicYear() {
        AcademicYear ay = new AcademicYear();
        ay.setId(ACADEMIC_YEAR_ID);
        ay.setName("2026-2027");
        ay.setIsActive(true);
        return ay;
    }

    private AcademicYearResponse buildAcademicYearResponse() {
        return new AcademicYearResponse(
                ACADEMIC_YEAR_ID,
                "2026-2027",
                "Description",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 5, 31),
                true
        );
    }

    private SchoolClassRequest buildRequest() {
        return new SchoolClassRequest(CLASS_NAME, ACADEMIC_YEAR_ID);
    }

    private SchoolClassUpdateRequest buildUpdateRequest() {
        return new SchoolClassUpdateRequest(UPDATED_CLASS_NAME);
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("успешно возвращает класс по ID")
        void success() {
            SchoolClass schoolClass = new SchoolClass();
            SchoolClassResponse response = new SchoolClassResponse(
                    CLASS_ID,
                    CLASS_NAME,
                    buildAcademicYearResponse(),
                    TEACHER_ID
            );

            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(schoolClass));
            when(schoolClassMapper.toSchoolClassResponse(schoolClass)).thenReturn(response);

            SchoolClassResponse result = service.findById(CLASS_ID);

            assertThat(result).isEqualTo(response);
            verify(schoolClassRepository).findById(CLASS_ID);
        }

        @Test
        @DisplayName("бросает NotFoundException, если класс не найден")
        void notFound() {
            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(CLASS_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass Not Found by id: " + CLASS_ID);

            verify(schoolClassRepository).findById(CLASS_ID);
        }
    }

    @Nested
    @DisplayName("findWithStudentById")
    class FindWithStudentById {
        @Test
        @DisplayName("собирает полную информацию о классе с учениками и учителем")
        void success() {
            ClassStudent classStudent = ClassStudent.builder().studentId(STUDENT_ID).build();
            SchoolClass schoolClass = SchoolClass.builder()
                    .id(CLASS_ID)
                    .name(CLASS_NAME)
                    .classTeacherId(TEACHER_ID)
                    .students(Set.of(classStudent))
                    .build();

            UserFeignResponse userResponse = new UserFeignResponse(STUDENT_ID, "Иван", "Иванов", "ivan", "key");
            TeacherResponse teacherResponse = new TeacherResponse(
                    new UserFeignResponse(TEACHER_ID, "Петр", "Петров", "petr", "key"),
                    new TeacherDetails("petr@mail.com", "123")
            );
            SchoolClassFullResponse expectedResponse = mock(SchoolClassFullResponse.class);

            when(schoolClassRepository.findWithClassStudentById(CLASS_ID)).thenReturn(Optional.of(schoolClass));
            when(userClient.getBatchUsers(List.of(STUDENT_ID))).thenReturn(new com.rusobr.academic.web.dto.feign.BatchUserResponse(List.of(userResponse), List.of()));
            when(userClient.getTeacherById(TEACHER_ID)).thenReturn(teacherResponse);
            when(schoolClassMapper.toSchoolClassFullResponse(schoolClass, new com.rusobr.academic.web.dto.feign.BatchUserResponse(List.of(userResponse), List.of()), teacherResponse, TEACHER_ID))
                    .thenReturn(expectedResponse);

            SchoolClassFullResponse result = service.findWithStudentsById(CLASS_ID);

            assertThat(result).isEqualTo(expectedResponse);
            verify(schoolClassRepository).findWithClassStudentById(CLASS_ID);
            verify(userClient).getBatchUsers(List.of(STUDENT_ID));
            verify(userClient).getTeacherById(TEACHER_ID);
        }

        @Test
        @DisplayName("возвращает пустой список пользователей если нет студентов")
        void success_noStudents() {
            SchoolClass schoolClass = SchoolClass.builder()
                    .id(CLASS_ID)
                    .name(CLASS_NAME)
                    .classTeacherId(TEACHER_ID)
                    .students(Set.of())
                    .build();

            TeacherResponse teacherResponse = new TeacherResponse(
                    new UserFeignResponse(TEACHER_ID, "Петр", "Петров", "petr", "key"),
                    new TeacherDetails("petr@mail.com", "123")
            );
            SchoolClassFullResponse expectedResponse = mock(SchoolClassFullResponse.class);

            when(schoolClassRepository.findWithClassStudentById(CLASS_ID)).thenReturn(Optional.of(schoolClass));
            when(userClient.getTeacherById(TEACHER_ID)).thenReturn(teacherResponse);
            when(schoolClassMapper.toSchoolClassFullResponse(schoolClass, new com.rusobr.academic.web.dto.feign.BatchUserResponse(List.of(), List.of()), teacherResponse, TEACHER_ID))
                    .thenReturn(expectedResponse);

            SchoolClassFullResponse result = service.findWithStudentsById(CLASS_ID);

            assertThat(result).isEqualTo(expectedResponse);
            verify(userClient, never()).getBatchUsers(any());
        }

        @Test
        @DisplayName("устанавливает teacher = null если classTeacherId = null")
        void success_noTeacher() {
            ClassStudent classStudent = ClassStudent.builder().studentId(STUDENT_ID).build();
            SchoolClass schoolClass = SchoolClass.builder()
                    .id(CLASS_ID)
                    .name(CLASS_NAME)
                    .classTeacherId(null)
                    .students(Set.of(classStudent))
                    .build();

            UserFeignResponse userResponse = new UserFeignResponse(STUDENT_ID, "Иван", "Иванов", "ivan", "key");
            SchoolClassFullResponse expectedResponse = mock(SchoolClassFullResponse.class);

            when(schoolClassRepository.findWithClassStudentById(CLASS_ID)).thenReturn(Optional.of(schoolClass));
            when(userClient.getBatchUsers(List.of(STUDENT_ID))).thenReturn(new com.rusobr.academic.web.dto.feign.BatchUserResponse(List.of(userResponse), List.of()));
            when(schoolClassMapper.toSchoolClassFullResponse(schoolClass, new com.rusobr.academic.web.dto.feign.BatchUserResponse(List.of(userResponse), List.of()), null, null))
                    .thenReturn(expectedResponse);

            SchoolClassFullResponse result = service.findWithStudentsById(CLASS_ID);

            assertThat(result).isEqualTo(expectedResponse);
            verify(userClient, never()).getTeacherById(any());
        }

        @Test
        @DisplayName("бросает NotFoundException если класс не найден")
        void notFound() {
            when(schoolClassRepository.findWithClassStudentById(CLASS_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findWithStudentsById(CLASS_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass Not Found by id: " + CLASS_ID);
        }
    }

    @Nested
    @DisplayName("findByStudent")
    class FindByStudent {
        @Test
        @DisplayName("возвращает класс по ID студента")
        void success() {
            SchoolClass schoolClass = SchoolClass.builder().build();
            SchoolClassResponse response = mock(SchoolClassResponse.class);

            when(schoolClassRepository.findSchoolClassByStudentId(STUDENT_ID)).thenReturn(Optional.of(schoolClass));
            when(schoolClassMapper.toSchoolClassResponse(schoolClass)).thenReturn(response);

            SchoolClassResponse result = service.findByStudent(STUDENT_ID);

            assertThat(result).isEqualTo(response);
            verify(schoolClassRepository).findSchoolClassByStudentId(STUDENT_ID);
        }

        @Test
        @DisplayName("бросает NotFoundException если класс для студента не найден")
        void notFound() {
            when(schoolClassRepository.findSchoolClassByStudentId(STUDENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByStudent(STUDENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass not found for student: " + STUDENT_ID);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {
        @Test
        @DisplayName("возвращает список всех классов отсортированных по имени")
        void success() {
            SchoolClass schoolClass1 = SchoolClass.builder().name("10A").build();
            SchoolClass schoolClass2 = SchoolClass.builder().name("10B").build();
            SchoolClassResponse response1 = mock(SchoolClassResponse.class);
            SchoolClassResponse response2 = mock(SchoolClassResponse.class);

            when(schoolClassRepository.findAllByOrderByNameAsc()).thenReturn(List.of(schoolClass1, schoolClass2));
            when(schoolClassMapper.toSchoolClassResponse(schoolClass1)).thenReturn(response1);
            when(schoolClassMapper.toSchoolClassResponse(schoolClass2)).thenReturn(response2);

            List<SchoolClassResponse> result = service.findAll();

            assertThat(result).containsExactly(response1, response2);
            verify(schoolClassRepository).findAllByOrderByNameAsc();
        }

        @Test
        @DisplayName("возвращает пустой список если нет классов")
        void success_empty() {
            when(schoolClassRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

            List<SchoolClassResponse> result = service.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByAcademicYear")
    class FindByAcademicYear {
        @Test
        @DisplayName("возвращает список классов по ID учебного года")
        void success() {
            SchoolClass schoolClass = SchoolClass.builder().build();
            SchoolClassResponse response = mock(SchoolClassResponse.class);

            when(schoolClassRepository.findAllByAcademicYearIdOrderByNameAsc(ACADEMIC_YEAR_ID))
                    .thenReturn(List.of(schoolClass));
            when(schoolClassMapper.toSchoolClassResponse(schoolClass)).thenReturn(response);

            List<SchoolClassResponse> result = service.findByAcademicYear(ACADEMIC_YEAR_ID);

            assertThat(result).containsExactly(response);
            verify(schoolClassRepository).findAllByAcademicYearIdOrderByNameAsc(ACADEMIC_YEAR_ID);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("успешно сохраняет новый класс")
        void success() {
            SchoolClassRequest request = buildRequest();
            AcademicYear academicYear = buildAcademicYear();
            SchoolClass schoolClass = new SchoolClass();
            SchoolClassResponse response = mock(SchoolClassResponse.class);

            when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(academicYear));
            when(schoolClassRepository.existsByNameAndAcademicYearId(CLASS_NAME, ACADEMIC_YEAR_ID)).thenReturn(false);
            when(schoolClassMapper.toSchoolClass(request, academicYear)).thenReturn(schoolClass);
            when(schoolClassRepository.save(schoolClass)).thenReturn(schoolClass);
            when(schoolClassMapper.toSchoolClassResponse(schoolClass)).thenReturn(response);

            SchoolClassResponse result = service.create(request);

            assertThat(result).isEqualTo(response);
            verify(schoolClassRepository).save(schoolClass);
            verify(academicYearRepository).findById(ACADEMIC_YEAR_ID);
        }

        @Test
        @DisplayName("бросает ConflictException если имя класса в учебном году уже занято")
        void conflict_alreadyExists() {
            SchoolClassRequest request = buildRequest();
            AcademicYear academicYear = buildAcademicYear();

            when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(academicYear));
            when(schoolClassRepository.existsByNameAndAcademicYearId(CLASS_NAME, ACADEMIC_YEAR_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");

            verify(schoolClassRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает NotFoundException если учебный год не найден")
        void notFound_academicYear() {
            SchoolClassRequest request = buildRequest();

            when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Academic year not found");

            verify(schoolClassRepository, never()).save(any());
        }

        @Test
        @DisplayName("бросает ConflictException если учебный год неактивен")
        void conflict_academicYearInactive() {
            SchoolClassRequest request = buildRequest();
            AcademicYear inactiveYear = buildAcademicYear();
            inactiveYear.setIsActive(false);

            when(academicYearRepository.findById(ACADEMIC_YEAR_ID)).thenReturn(Optional.of(inactiveYear));

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic year is not active");

            verify(schoolClassRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("assignTeacher")
    class AssignTeacher {
        @Test
        @DisplayName("делегирует работу транзакционному методу после проверки учителя")
        void success() {
            SchoolClass schoolClass = SchoolClass.builder()
                    .id(CLASS_ID)
                    .build();
            AcademicYear academicYear = buildAcademicYear();
            schoolClass.setAcademicYear(academicYear);

            when(userClient.getTeacherById(TEACHER_ID)).thenReturn(mock(TeacherResponse.class));
            when(schoolClassRepository.findWithAcademicYearById(CLASS_ID)).thenReturn(Optional.of(schoolClass));

            service.assignTeacher(CLASS_ID, TEACHER_ID);

            assertThat(schoolClass.getClassTeacherId()).isEqualTo(TEACHER_ID);
            verify(userClient).getTeacherById(TEACHER_ID);
        }

        @Test
        @DisplayName("бросает исключение если учитель не найден")
        void notFound_teacher() {
            when(userClient.getTeacherById(TEACHER_ID)).thenThrow(new NotFoundException("Teacher not found"));

            assertThatThrownBy(() -> service.assignTeacher(CLASS_ID, TEACHER_ID))
                    .isInstanceOf(NotFoundException.class);

            verify(schoolClassRepository, never()).findWithAcademicYearById(any());
        }

        @Test
        @DisplayName("бросает NotFoundException если класс не найден")
        void notFound_class() {
            when(userClient.getTeacherById(TEACHER_ID)).thenReturn(mock(TeacherResponse.class));
            when(schoolClassRepository.findWithAcademicYearById(CLASS_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.assignTeacher(CLASS_ID, TEACHER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass Not Found by id: " + CLASS_ID);
        }

        @Test
        @DisplayName("бросает ConflictException если учебный год неактивен")
        void conflict_academicYearInactive() {
            SchoolClass schoolClass = SchoolClass.builder().id(CLASS_ID).build();
            AcademicYear inactiveYear = buildAcademicYear();
            inactiveYear.setIsActive(false);
            schoolClass.setAcademicYear(inactiveYear);

            when(userClient.getTeacherById(TEACHER_ID)).thenReturn(mock(TeacherResponse.class));
            when(schoolClassRepository.findWithAcademicYearById(CLASS_ID)).thenReturn(Optional.of(schoolClass));

            assertThatThrownBy(() -> service.assignTeacher(CLASS_ID, TEACHER_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic year is not active");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("успешно обновляет имя класса")
        void success() {
            SchoolClassUpdateRequest request = buildUpdateRequest();
            SchoolClass schoolClass = SchoolClass.builder()
                    .id(CLASS_ID)
                    .name(CLASS_NAME)
                    .build();
            AcademicYear academicYear = buildAcademicYear();
            schoolClass.setAcademicYear(academicYear);

            when(schoolClassRepository.findWithAcademicYearById(CLASS_ID)).thenReturn(Optional.of(schoolClass));

            service.update(CLASS_ID, request);

            assertThat(schoolClass.getName()).isEqualTo(UPDATED_CLASS_NAME);
            verify(schoolClassRepository).findWithAcademicYearById(CLASS_ID);
        }

        @Test
        @DisplayName("бросает NotFoundException если класс не найден")
        void notFound_class() {
            SchoolClassUpdateRequest request = buildUpdateRequest();

            when(schoolClassRepository.findWithAcademicYearById(CLASS_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(CLASS_ID, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass Not Found by id: " + CLASS_ID);
        }

        @Test
        @DisplayName("бросает ConflictException если учебный год неактивен")
        void conflict_academicYearInactive() {
            SchoolClassUpdateRequest request = buildUpdateRequest();
            SchoolClass schoolClass = SchoolClass.builder().id(CLASS_ID).build();
            AcademicYear inactiveYear = buildAcademicYear();
            inactiveYear.setIsActive(false);
            schoolClass.setAcademicYear(inactiveYear);

            when(schoolClassRepository.findWithAcademicYearById(CLASS_ID)).thenReturn(Optional.of(schoolClass));

            assertThatThrownBy(() -> service.update(CLASS_ID, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic year is not active");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("успешно удаляет класс")
        void success() {
            SchoolClass schoolClass = SchoolClass.builder().id(CLASS_ID).build();
            AcademicYear academicYear = buildAcademicYear();
            schoolClass.setAcademicYear(academicYear);

            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(schoolClass));

            service.delete(CLASS_ID);

            verify(schoolClassRepository).delete(schoolClass);
        }

        @Test
        @DisplayName("бросает NotFoundException если класс не найден")
        void notFound() {
            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(CLASS_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("SchoolClass Not Found by id: " + CLASS_ID);

            verify(schoolClassRepository, never()).delete(any());
        }

        @Test
        @DisplayName("бросает ConflictException если учебный год неактивен")
        void conflict_academicYearInactive() {
            SchoolClass schoolClass = SchoolClass.builder().id(CLASS_ID).build();
            AcademicYear inactiveYear = buildAcademicYear();
            inactiveYear.setIsActive(false);
            schoolClass.setAcademicYear(inactiveYear);

            when(schoolClassRepository.findById(CLASS_ID)).thenReturn(Optional.of(schoolClass));

            assertThatThrownBy(() -> service.delete(CLASS_ID))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Academic year is not active");

            verify(schoolClassRepository, never()).delete(any());
        }
    }
}

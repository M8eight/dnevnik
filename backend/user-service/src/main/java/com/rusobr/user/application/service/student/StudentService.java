package com.rusobr.user.application.service.student;

import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.web.dto.feign.BatchUserResponse;
import com.rusobr.user.web.dto.student.StudentInfoResponse;
import com.rusobr.user.web.exception.ConflictException;
import com.rusobr.user.web.exception.ExceptionCode;
import com.rusobr.user.web.exception.NotFoundException;
import com.rusobr.user.infrastructure.client.feign.AcademicClient;
import com.rusobr.user.application.mapper.StudentMapper;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.application.service.teacher.TeacherService;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;
    private final AcademicClient academicClient;
    private final TeacherService teacherService;
    private final ParentRepository parentRepository;
    private final UserMapper userMapper;

    @Lazy
    @Autowired
    private StudentService self;

    @Transactional(readOnly = true)
    public BatchUserResponse getBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new BatchUserResponse(List.of(), List.of());
        }

        List<UserFeignResponse> students = studentRepository.findAllStudentsByIds(ids).stream()
                .map(userMapper::toUserFeignResponse).toList();

        List<Long> foundIds = students.stream().map(UserFeignResponse::id).toList();
        List<Long> notFound = ids.stream().filter(id -> !foundIds.contains(id)).toList();

        return new BatchUserResponse(students, notFound);
    }

    @Transactional(readOnly = true)
    public List<UserFeignResponse> getBatchWithExcludingIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return studentRepository.findWithUserAllStudents().stream().map(userMapper::toUserFeignResponse).toList();
        }

        return studentRepository.findAllStudentsExcludeAssigned(ids).stream().map(userMapper::toUserFeignResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudentDetails getDetailsById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> notFoundStudent(id));
        return studentMapper.toStudentDetails(student);
    }

    public StudentWithClassResponse getWithClassById(Long id) {
        Student student = self.getStudentTransactional(id);
        SchoolClassResponse schoolClass = academicClient.getSchoolClassByStudentId(student.getId());
        TeacherResponse teacher = teacherService.getWithUserById(schoolClass.classTeacherId());

        return studentMapper.toStudentDetailResponse(student, schoolClass, teacher);
    }

    @Transactional(readOnly = true)
    Student getStudentTransactional(Long id) {
        return studentRepository.findWithUserById(id)
                .orElseThrow(() -> notFoundStudent(id));
    }

    public StudentInfoResponse getStudentInfoById(Long id) {
        Student student = self.getStudentInfoTransactional(id);
        SchoolClassResponse schoolClass = academicClient.getSchoolClassByStudentId(student.getId());
        TeacherResponse teacher = teacherService.getWithUserById(schoolClass.classTeacherId());

        return studentMapper.toStudentInfoResponse(student, schoolClass, teacher);
    }

    @Transactional(readOnly = true)
    Student getStudentInfoTransactional(Long id) {
        return studentRepository.findStudentInfoById(id)
                .orElseThrow(() -> notFoundStudent(id));
    }

    public Optional<Student> findByIdWithDeleted(Long id) {
        return studentRepository.findByIdWithDeleted(id);
    }

    @Transactional
    public void create(Long userId, StudentDetails studentDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> notFoundUser(userId));
        studentRepository.save(studentMapper.toEntity(user, studentDetails));
    }

    @Transactional
    public void update(Long userId, StudentDetails studentDetails) {
        if (!userRepository.existsById(userId)) {
            throw notFoundUser(userId);
        }
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> notFoundStudent(userId));

        if (studentDetails.studyProfile() != null) {
            student.setStudyProfile(studentDetails.studyProfile());
        }
    }

    @Transactional
    public void assignToParent(Long studentId, Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> notFoundParent(parentId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> notFoundStudent(studentId));

        if (student.getParent() != null) {
            throw conflictStudentAlreadyHasParent(studentId);
        }

        student.setParent(parent);
        studentRepository.save(student);
    }

    @Transactional
    public void unassignFromParent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> notFoundStudent(studentId));

        if (student.getParent() == null) {
            throw conflictStudentHasNoParent(studentId);
        }

        student.setParent(null);
        studentRepository.save(student);
    }

    public void delete(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw notFoundStudent(studentId);
        }
        studentRepository.deleteById(studentId);
    }

    @EventListener
    public void handleUserDelete(UserDeletedEvent event) {
        if (event.roles().contains(UserRole.STUDENT)) {
            this.delete(event.id());
        }
    }

    // helpers
    private NotFoundException notFoundStudent(Long id) {
        return new NotFoundException("Student by id: %d not found".formatted(id), ExceptionCode.STUDENT_NOT_FOUND);
    }

    private NotFoundException notFoundUser(Long id) {
        return new NotFoundException("User by id: %d not found".formatted(id), ExceptionCode.USER_NOT_FOUND);
    }

    private NotFoundException notFoundParent(Long id) {
        return new NotFoundException("Parent by id: %d not found".formatted(id), ExceptionCode.PARENT_NOT_FOUND);
    }

    private ConflictException conflictStudentAlreadyHasParent(Long studentId) {
        return new ConflictException("Student by id: %d already has parent".formatted(studentId), ExceptionCode.STUDENT_ALREADY_HAS_PARENT);
    }

    private ConflictException conflictStudentHasNoParent(Long studentId) {
        return new ConflictException("Student by id: %d has no parent".formatted(studentId), ExceptionCode.STUDENT_HAS_NO_PARENT);
    }
}
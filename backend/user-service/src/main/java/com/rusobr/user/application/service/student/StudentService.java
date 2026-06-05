package com.rusobr.user.application.service.student;

import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.domain.enums.UserRole;
import com.rusobr.user.web.exception.ConflictException;
import com.rusobr.user.web.exception.NotFoundException;
import com.rusobr.user.infrastructure.client.feign.SchoolClassClient;
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
    private final SchoolClassClient schoolClassClient;
    private final TeacherService teacherService;
    private final ParentRepository parentRepository;
    private final UserMapper userMapper;

    public List<UserFeignResponse> getBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return studentRepository.findAllStudentsByIds(ids).stream().map(userMapper::toUserFeignResponse).toList();
    }

    public List<UserFeignResponse> getBatchWithExcludingIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return studentRepository.findWithUserAllStudents().stream().map(userMapper::toUserFeignResponse).toList();
        }

        return studentRepository.findAllStudentsExcludeAssigned(ids).stream().map(userMapper::toUserFeignResponse).toList();
    }

    public StudentDetails getDetailsById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found: " + id));
        return studentMapper.toStudentDetails(student);
    }

    public StudentWithClassResponse getWithClassById(Long id) {
        Student student = studentRepository.findWithUserById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        SchoolClassResponse schoolClass = schoolClassClient.getSchoolClassByStudentId(student.getId());
        TeacherResponse teacher = teacherService.getWithUserById(schoolClass.classTeacherId());

        return studentMapper.toStudentDetailResponse(student, schoolClass, teacher);
    }

    public Optional<Student> findByIdWithDeleted(Long id) {
        return studentRepository.findByIdWithDeleted(id);
    }

    public void create(Long userId, StudentDetails studentDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        studentRepository.save(studentMapper.toEntity(user, studentDetails));
    }

    @Transactional
    public void update(Long userId, StudentDetails studentDetails) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Student not found: " + userId));

        if (studentDetails.studyProfile() != null) {
            student.setStudyProfile(studentDetails.studyProfile());
        }
    }

    @Transactional
    public void assignToParent(Long studentId, Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent not found: " + parentId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        if (student.getParent() != null && student.getParent().getId().equals(parentId)) {
            throw new ConflictException("Student already has parent");
        }
        if (student.getParent() != null) {
            throw new ConflictException("Student already set parent");
        }

        student.setParent(parent);
        studentRepository.save(student);
    }

    @Transactional
    public void unassignFromParent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        if (student.getParent() == null) {
            throw new ConflictException("Student has no parent");
        }

        student.setParent(null);
        studentRepository.save(student);
    }

    public void delete(Long studentId) {
        studentRepository.deleteById(studentId);
    }

    @EventListener
    public void handleUserDelete(UserDeletedEvent event) {
        if (event.roles().contains(UserRole.STUDENT)) {
            this.delete(event.id());
        }
    }

}

package com.rusobr.user.infrastructure.service.student;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.feignClient.SchoolClassClient;
import com.rusobr.user.infrastructure.mapper.StudentMapper;
import com.rusobr.user.infrastructure.mapper.UserMapper;
import com.rusobr.user.infrastructure.persistence.repository.ParentRepository;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.service.teacher.TeacherService;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    public List<UserFeignResponse> findSimpleBatchStudents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return studentRepository.findAllStudentsByIds(ids);
    }

    public List<UserFeignResponse> getStudentsExcludingIds(Set<Long> ids) {
        if (ids.isEmpty()) {
            return studentRepository.findWithUserAllStudents();
        }

        return studentRepository.findAllStudentsExcludeAssigned(ids);
    }

    public StudentDetails findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found: " + id));
        return studentMapper.toStudentDetails(student);
    }

    public StudentWithClassResponse findStudentDetailById(Long id) {
        Student student = studentRepository.findWithUserById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        SchoolClassResponse schoolClass = schoolClassClient.getSchoolClassByStudentId(student.getId());
        TeacherResponse teacher = teacherService.findWithUserById(schoolClass.classTeacherId());

        return studentMapper.toStudentDetailResponse(student, schoolClass, teacher);
    }

    public Optional<Student> findByIdWithDeleted(Long id) {
        return studentRepository.findByIdWithDeleted(id);
    }

    public void createStudent(Long userId, StudentDetails studentDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        studentRepository.save(studentMapper.toEntity(user, studentDetails));
    }

    @Transactional
    public void updateStudent(Long userId, StudentDetails studentDetails) {
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
    public void assignStudentToParent(Long studentId, Long parentId) {
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
    public void unassignStudentFromParent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        if (student.getParent() == null) {
            throw new ConflictException("Student has no parent");
        }

        student.setParent(null);
        studentRepository.save(student);
    }

    public void deleteById(Long studentId) {
        studentRepository.deleteById(studentId);
    }

}

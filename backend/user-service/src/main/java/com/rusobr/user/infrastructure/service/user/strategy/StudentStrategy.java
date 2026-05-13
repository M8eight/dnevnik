package com.rusobr.user.infrastructure.service.user.strategy;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.service.student.StudentService;
import com.rusobr.user.infrastructure.service.user.UserProfileDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StudentStrategy implements UserRoleStrategy {

    private final StudentService studentService;

    @Override
    public void save(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof StudentDetails studentDetails) {
            Optional<Student> studentOptional = studentService.findByIdWithDeleted(userId);
            if (studentOptional.isPresent()) {
                Student student = studentOptional.get();
                student.setDeletedAt(null);
                student.setStudyProfile(studentDetails.studyProfile());
            } else {
                studentService.createStudent(userId, studentDetails);
            }
        } else {
            throw new ConflictException("Invalid user profile details");
        }
    }

    @Override
    public void delete(Long userId) {
        studentService.deleteById(userId);
    }

    @Override
    public void update(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof StudentDetails studentDetails) {
            studentService.updateStudent(userId, studentDetails);
        } else {
            throw new ConflictException("Invalid user profile details for update");
        }
    }

    @Override
    public UserRole getRole() {
        return UserRole.STUDENT;
    }

}

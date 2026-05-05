package com.rusobr.user.infrastructure.service.student;

import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.service.user.CreateUserStrategy;
import com.rusobr.user.infrastructure.service.user.UserProfileDetails;
import com.rusobr.user.web.dto.student.StudentDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StudentCreateStrategy implements CreateUserStrategy {

    private final StudentService studentService;

    @Override
    public void save(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof StudentDetails studentDetails) {
            studentService.createStudent(userId, studentDetails);
        } else {
            throw new ConflictException("Invalid user profile details");
        }
    }

    @Override
    public UserRole getRole() {
        return UserRole.STUDENT;
    }

}

package com.rusobr.user.infrastructure.service.user.strategy;

import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.service.teacher.TeacherService;
import com.rusobr.user.infrastructure.service.user.UserProfileDetails;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TeacherStrategy implements UserRoleStrategy {
    private final TeacherService teacherService;

    @Override
    public void save(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof TeacherDetails teacherDetails) {
            teacherService.createTeacher(userId, teacherDetails);
        } else {
            throw new ConflictException("Invalid user profile details");
        }
    }

    @Override
    public void delete(Long userId) {
        teacherService.deleteById(userId);
    }

    @Override
    public void update(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof TeacherDetails teacherDetails) {
            teacherService.updateTeacher(userId, teacherDetails);
        } else {
            throw new ConflictException("Invalid user profile details");
        }
    }

    @Override
    public UserRole getRole() {
        return UserRole.TEACHER;
    }

}

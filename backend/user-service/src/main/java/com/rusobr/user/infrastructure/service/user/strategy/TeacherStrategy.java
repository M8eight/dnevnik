package com.rusobr.user.infrastructure.service.user.strategy;

import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.ConflictException;
import com.rusobr.user.infrastructure.service.teacher.TeacherService;
import com.rusobr.user.web.dto.user.UserProfileDetails;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.user.UserRoleStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class TeacherStrategy implements UserRoleStrategy {

    private final TeacherService teacherService;

    @Override
    public void save(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof TeacherDetails teacherDetails) {
            Optional<Teacher> teacherOptional = teacherService.findByIdWithDeleted(userId);
            if (teacherOptional.isPresent()) {
                Teacher teacher = teacherOptional.get();
                teacher.setDeletedAt(null);
                teacher.setPhoneNumber(teacherDetails.phoneNumber());
                teacher.setEmail(teacherDetails.email());
            } else {
                teacherService.create(userId, teacherDetails);
            }
        } else {
            throw new ConflictException("Invalid user profile details");
        }
    }

    @Override
    public void delete(Long userId) {
        teacherService.delete(userId);
    }

    @Override
    public void update(Long userId, UserProfileDetails userDetails) {
        if (userDetails instanceof TeacherDetails teacherDetails) {
            teacherService.update(userId, teacherDetails);
        } else {
            throw new ConflictException("Invalid user profile details");
        }
    }

    @Override
    public UserRole getRole() {
        return UserRole.TEACHER;
    }

}

package com.rusobr.user.infrastructure.service.teacher;

import com.rusobr.user.domain.event.UserDeletedEvent;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.enums.UserRole;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.mapper.TeacherMapper;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.feign.UserResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final UserRepository userRepository;

    public TeacherResponse getWithUserById(Long id) {
        Teacher teacher = teacherRepository.findWithUserById(id).orElseThrow(() -> new NotFoundException("Teacher with id " + id + " not found"));
        return teacherMapper.toTeacherResponse(teacher);
    }

    public TeacherDetails getDetailsById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Teacher not found: " + id));
        return teacherMapper.toTeacherDetails(teacher);
    }

    public List<UserResponse> getBatch(List<Long> ids) {
        return teacherRepository.findAllTeachersByIds(ids);
    }

    public UserResponse getSimpleById(Long id) {
        return teacherRepository.getTeacherSimpleById(id);
    }

    public Optional<Teacher> findByIdWithDeleted(Long id) {
        return teacherRepository.findByIdWithDeleted(id);
    }

    public void create(Long userId, TeacherDetails teacherDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        teacherRepository.save(teacherMapper.toEntity(user, teacherDetails));
    }

    @Transactional
    public void update(Long userId, TeacherDetails teacherDetails) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        Teacher teacher = teacherRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Teacher not found: " + userId));

        if (teacherDetails.phoneNumber() != null) {
            teacher.setPhoneNumber(teacherDetails.phoneNumber());
        }
        if (teacherDetails.email() != null) {
            teacher.setEmail(teacherDetails.email());
        }
    }

    public void delete(Long id) {
        teacherRepository.deleteById(id);
    }

    @EventListener
    public void handleUserDelete(UserDeletedEvent event) {
        if (event.roles().contains(UserRole.TEACHER)) {
            this.delete(event.id());
        }
    }


}

package com.rusobr.user.application.service.teacher;

import com.rusobr.common.enums.UserRole;
import com.rusobr.common.exception.NotFoundException;
import com.rusobr.user.application.event.UserDeletedEvent;
import com.rusobr.user.application.mapper.TeacherMapper;
import com.rusobr.user.application.mapper.UserMapper;
import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.client.feign.AcademicClient;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.feign.BatchUserResponse;
import com.rusobr.user.web.dto.feign.TeacherAcademicFeignDto;
import com.rusobr.user.web.dto.feign.UserFeignResponse;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherInfoResponse;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.exception.UserExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private final UserMapper userMapper;
    private final AcademicClient academicClient;

    @Lazy
    @Autowired
    private TeacherService self;

    @Transactional(readOnly = true)
    public TeacherResponse getWithUserById(Long id) {
        Teacher teacher = teacherRepository.findWithUserById(id)
                .orElseThrow(() -> notFoundTeacher(id));
        return teacherMapper.toTeacherResponse(teacher);
    }

    @Transactional(readOnly = true)
    public TeacherDetails getDetailsById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> notFoundTeacher(id));
        return teacherMapper.toTeacherDetails(teacher);
    }

    @Transactional(readOnly = true)
    public BatchUserResponse getBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new BatchUserResponse(List.of(), List.of());
        }

        List<UserFeignResponse> teachers = teacherRepository.findAllTeachersByIds(ids).stream()
                .map(userMapper::toUserFeignResponse).toList();
        List<Long> foundIds = teachers.stream().map(UserFeignResponse::id).toList();
        List<Long> notFound = ids.stream().filter(id -> !foundIds.contains(id)).toList();

        return new BatchUserResponse(teachers, notFound);
    }

    @Transactional(readOnly = true)
    public UserFeignResponse getSimpleById(Long id) {
        return userMapper.toUserFeignResponse(teacherRepository.getTeacherSimpleById(id));
    }

    public TeacherInfoResponse getInfoById(Long id) {
        TeacherAcademicFeignDto teacherInfoFeignResponse = academicClient.getTeacherAcademicInfo(id);
        return self.getInfoByIdTransactional(id, teacherInfoFeignResponse);
    }

    @Transactional(readOnly = true)
    public TeacherInfoResponse getInfoByIdTransactional(Long id, TeacherAcademicFeignDto dto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> notFoundTeacher(id));

        return TeacherInfoResponse.builder()
                .phoneNumber(teacher.getPhoneNumber())
                .email(teacher.getEmail())
                .schoolDetails(dto)
                .build();
    }

    public Optional<Teacher> findByIdWithDeleted(Long id) {
        return teacherRepository.findByIdWithDeleted(id);
    }

    @Transactional
    public void create(Long userId, TeacherDetails teacherDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> notFoundUser(userId));
        teacherRepository.save(teacherMapper.toEntity(user, teacherDetails));
    }

    @Transactional
    public void update(Long userId, TeacherDetails teacherDetails) {
        if (!userRepository.existsById(userId)) {
            throw notFoundUser(userId);
        }
        Teacher teacher = teacherRepository.findById(userId)
                .orElseThrow(() -> notFoundTeacher(userId));

        if (teacherDetails.phoneNumber() != null) {
            teacher.setPhoneNumber(teacherDetails.phoneNumber());
        }
        if (teacherDetails.email() != null) {
            teacher.setEmail(teacherDetails.email());
        }
    }

    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw notFoundTeacher(id);
        }
        teacherRepository.deleteById(id);
    }

    @EventListener
    public void handleUserDelete(UserDeletedEvent event) {
        if (event.roles().contains(UserRole.TEACHER)) {
            this.delete(event.id());
        }
    }

    // helpers
    private NotFoundException notFoundTeacher(Long id) {
        return new NotFoundException("Teacher by id: %d not found".formatted(id), UserExceptionCode.TEACHER_NOT_FOUND);
    }

    private NotFoundException notFoundUser(Long id) {
        return new NotFoundException("User by id: %d not found".formatted(id), UserExceptionCode.USER_NOT_FOUND);
    }
}
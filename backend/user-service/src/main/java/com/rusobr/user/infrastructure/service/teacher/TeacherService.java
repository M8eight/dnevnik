package com.rusobr.user.infrastructure.service.teacher;

import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.mapper.TeacherMapper;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.web.dto.teacher.TeacherDetails;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final UserRepository userRepository;

    public TeacherResponse findWithUserById(Long id) {
        Teacher teacher = teacherRepository.findWithUserById(id).orElseThrow(() -> new NotFoundException("Teacher with id " + id + " not found"));
        return teacherMapper.toTeacherResponse(teacher);
    }

    public TeacherDetails findDetailsById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Teacher not found: " + id));
        return teacherMapper.toTeacherDetails(teacher);
    }

    public Optional<Teacher> findByIdWithDeleted(Long id) {
        return teacherRepository.findByIdWithDeleted(id);
    }

    public void createTeacher(Long userId, TeacherDetails teacherDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        teacherRepository.save(teacherMapper.toEntity(user, teacherDetails));
    }

    @Transactional
    public void updateTeacher(Long userId, TeacherDetails teacherDetails) {
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

    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }


}

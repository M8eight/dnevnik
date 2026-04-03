package com.rusobr.user.infrastructure.service;

import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.infrastructure.mapper.TeacherMapper;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    public TeacherResponse findWithUserById(Long id) {
        log.info("findWithUserById={}", id);
        Optional<Teacher> teacherOptional = teacherRepository.findWithUserById(id);
        log.info("teacherOptional={}", teacherOptional);
        return teacherMapper.toTeacherResponse(teacherOptional.orElse(null));
    }

    //todo crud сделать

    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }


}

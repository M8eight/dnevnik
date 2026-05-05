package com.rusobr.user.infrastructure.service.teacher;

import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.mapper.TeacherMapper;
import com.rusobr.user.infrastructure.persistence.repository.TeacherRepository;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    public TeacherResponse findWithUserById(Long id) {
        Teacher teacher = teacherRepository.findWithUserById(id).orElseThrow(() -> new NotFoundException("Teacher with id " + id + " not found"));
        return teacherMapper.toTeacherResponse(teacher);
    }

    //todo crud сделать

    public void deleteById(Long id) {
        teacherRepository.deleteById(id);
    }


}

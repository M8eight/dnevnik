package com.rusobr.service.domain.service;

import com.rusobr.service.web.dto.RequestTeacherDto;
import com.rusobr.service.domain.model.Teacher;
import com.rusobr.service.infrastructure.mapper.TeacherMapper;
import com.rusobr.service.infrastructure.persistence.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;

    public Iterable<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    public Teacher get(Long id) {
        return teacherRepository.findById(id).orElse(null);
    }

    public Teacher create(RequestTeacherDto teacher) {
        Teacher teacherEntity = teacherMapper.toTeacher(teacher);
        return teacherRepository.save(teacherEntity);
    }

    //TODO update method сделать

    public void delete(Long id) {
        teacherRepository.deleteById(id);
    }

}

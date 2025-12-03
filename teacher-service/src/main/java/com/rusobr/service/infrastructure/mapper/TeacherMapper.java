package com.rusobr.service.infrastructure.mapper;

import com.rusobr.service.web.dto.RequestTeacherDto;
import com.rusobr.service.domain.model.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TeacherMapper {
    Teacher toTeacher(RequestTeacherDto requestTeacherDto);
    RequestTeacherDto toRequestTeacherDto(Teacher teacher);
}


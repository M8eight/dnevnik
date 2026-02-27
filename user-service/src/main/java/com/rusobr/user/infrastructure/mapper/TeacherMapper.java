package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.Teacher;
import com.rusobr.user.web.dto.teacher.TeacherRequestDto;
import com.rusobr.user.web.dto.teacher.TeacherResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TeacherMapper {
    TeacherResponseDto toTeacherResponseDto(Teacher teacher);
    Teacher toTeacher(TeacherRequestDto teacherRequestDto);
    void updateEntityFromDto(TeacherRequestDto dto, @MappingTarget Teacher teacher);
}

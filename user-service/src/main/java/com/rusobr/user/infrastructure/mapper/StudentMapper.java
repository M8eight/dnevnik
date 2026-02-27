package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.web.dto.student.StudentRequestDto;
import com.rusobr.user.web.dto.student.StudentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StudentMapper {
    StudentResponseDto toStudentResponseDto(Student student);
    Student toStudent(StudentRequestDto studentRequestDto);
    void updateEntityFromDto(StudentRequestDto dto, @MappingTarget Student student);
}

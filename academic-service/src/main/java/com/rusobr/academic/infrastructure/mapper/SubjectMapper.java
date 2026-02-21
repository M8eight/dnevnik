package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.web.dto.subject.SubjectRequestDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubjectMapper {
    SubjectResponseDto toSubjectResponseDto(Subject subject);
    Subject toSubject(SubjectRequestDto createSubjectRequestDto);
    void updateEntityFromDto(SubjectRequestDto dto, @MappingTarget Subject subject);
}

package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.web.dto.subject.SubjectRequestDto;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {
    SubjectResponseDto toSubjectResponseDto(Subject subject);

    @Mapping(target = "name", source = "subjectName")
    Subject toSubject(SubjectRequestDto createSubjectRequestDto);

    @Mapping(target = "name", source = "subjectName")
    void updateEntityFromDto(SubjectRequestDto dto, @MappingTarget Subject subject);
}

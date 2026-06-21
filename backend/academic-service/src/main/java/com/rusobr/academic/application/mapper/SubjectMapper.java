package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.infrastructure.persistence.projection.SubjectResponseProjection;
import com.rusobr.academic.web.dto.subject.SubjectRequest;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {

    SubjectResponseDto toSubjectResponseDto(Subject subject);

    @Mapping(target = "name", source = "subjectName")
    Subject toSubject(SubjectRequest createSubjectRequest);

    @Mapping(target = "name", source = "subjectName")
    void updateEntityFromDto(SubjectRequest dto, @MappingTarget Subject subject);

    SubjectResponseDto toSubjectResponseDto(SubjectResponseProjection projection);

}

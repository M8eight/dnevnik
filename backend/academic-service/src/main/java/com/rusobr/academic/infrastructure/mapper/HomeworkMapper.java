package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HomeworkMapper {
    HomeworkResponse toHomeworkResponse(Homework homework);
}

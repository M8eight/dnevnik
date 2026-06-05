package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.Homework;
import com.rusobr.academic.infrastructure.persistence.projection.HomeworkWithSubjectProjection;
import com.rusobr.academic.web.dto.homework.HomeworkDiaryResponse;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import com.rusobr.academic.web.dto.homework.HomeworkWithSubjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HomeworkMapper {

    HomeworkResponse toHomeworkResponse(Homework homework);

    HomeworkDiaryResponse toDiaryHomeworkResponse(Homework homework);

    HomeworkWithSubjectResponse toWithSubjectResponse(HomeworkWithSubjectProjection projection);

}

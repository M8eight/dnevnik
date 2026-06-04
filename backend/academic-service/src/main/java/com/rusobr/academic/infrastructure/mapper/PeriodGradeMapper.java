package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeStudentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PeriodGradeMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "academicPeriodId", source = "academicPeriod.id")
    PeriodGradeResponse toPeriodGradeResponse(PeriodGrade periodGrade);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "subjectName", source = "teachingAssignment.subject.name")
    @Mapping(target = "academicPeriodId", source = "academicPeriod.id")
    PeriodGradeStudentResponse toPeriodGradeStudentResponse(PeriodGrade periodGrade);
}

package com.rusobr.academic.infrastructure.mapper;


import com.rusobr.academic.domain.model.FinalGrade;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeCreateResponse;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeRequest;
import com.rusobr.academic.web.dto.grade.finalGrade.FinalGradeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinalGradeMapper {
    FinalGrade toFinalGrade(FinalGradeRequest finalGradeRequest);
    FinalGradeCreateResponse toFinalGradeCreateResponse(FinalGrade finalGrade);

    @Mapping(target = "subjectName", source = "teachingAssignment.subject.name")
    FinalGradeResponse toFinalGradeResponse(FinalGrade finalGrade);
}

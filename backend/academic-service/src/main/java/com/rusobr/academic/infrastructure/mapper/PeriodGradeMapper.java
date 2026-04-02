package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.PeriodGrade;
import com.rusobr.academic.web.dto.grade.periodGrade.PeriodGradeResponse;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeProjection;
import com.rusobr.academic.web.dto.grade.periodGrade.StudentPeriodGradeResponse;
import com.rusobr.academic.web.dto.userService.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PeriodGradeMapper {
    PeriodGradeResponse toPeriodGradeResponse(PeriodGrade periodGrade);

    @Mapping(source = "studentPeriodGradeProjection.studentId", target = "studentId")
    @Mapping(source = "userResponse.firstName", target = "firstName")
    @Mapping(source = "userResponse.lastName", target = "lastName")
    @Mapping(source = "studentPeriodGradeProjection.value", target = "value")
    @Mapping(source = "studentPeriodGradeProjection.description", target = "description")
    @Mapping(source = "studentPeriodGradeProjection.gradeId", target = "gradeId")
    StudentPeriodGradeResponse toStudentPeriodGradeResponse(StudentPeriodGradeProjection studentPeriodGradeProjection,
                                                     UserResponse userResponse);
}

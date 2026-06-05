package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.TeacherSubject;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeacherSubjectMapper {
    TeacherSubjectResponse toResponse(TeacherSubject teacherSubject, UserFeignResponse teacher);
}

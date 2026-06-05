package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolClassProjection;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SchoolClassMapper {

    SchoolClassResponse toSchoolClassResponse(SchoolClass schoolClass);

    SchoolClass toSchoolClass(SchoolClassRequest schoolClassReq);

    void updateSchoolClass(@MappingTarget SchoolClass schoolClass, SchoolClassRequest request);
    @Mapping(target = "teacher", source = "teacher")
    @Mapping(target = "students", source = "users")
    SchoolClassFullResponse toSchoolClassFullResponse(SchoolClass schoolClass, List<UserFeignResponse> users,
                                                      TeacherResponse teacher);

    SchoolClassResponse toSchoolClassResponse(SchoolClassProjection projection);

}

package com.rusobr.academic.application.mapper;

import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.SchoolClass;
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

    @Mapping(target = "academicYear.id", source = "academicYear.id")
    @Mapping(target = "name", source = "schoolClassReq.name")
    @Mapping(target = "id", ignore = true)
    SchoolClass toSchoolClass(SchoolClassRequest schoolClassReq, AcademicYear academicYear);

    @Mapping(target = "teacher", source = "teacher")
    @Mapping(target = "students", source = "users")
    SchoolClassFullResponse toSchoolClassFullResponse(SchoolClass schoolClass, List<UserFeignResponse> users,
                                                      TeacherResponse teacher);

}

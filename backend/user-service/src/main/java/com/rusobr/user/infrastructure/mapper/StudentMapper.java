package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.student.StudentResponseDetail;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMapper {

    @Mapping(target = "id", source = "student.id")
    @Mapping(target = "keycloakId", source = "student.user.keycloakId")
    @Mapping(target = "firstName", source = "student.user.firstName")
    @Mapping(target = "lastName", source = "student.user.lastName")
    @Mapping(target = "studyProfile", source = "student.studyProfile")
    @Mapping(target = "schoolClass", source = "schoolClass")
    @Mapping(target = "schoolClassTeacher", source = "schoolClassTeacher")
    StudentResponseDetail toStudentResponse(Student student, SchoolClassResponse schoolClass,
                                            TeacherResponse  schoolClassTeacher);

}

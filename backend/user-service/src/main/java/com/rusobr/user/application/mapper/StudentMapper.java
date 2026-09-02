package com.rusobr.user.application.mapper;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentWithClassResponse;
import com.rusobr.user.web.dto.student.StudentWithParentDto;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMapper {

    @Mapping(target = "id", source = "student.id")
    @Mapping(target = "firstName", source = "student.user.firstName")
    @Mapping(target = "lastName", source = "student.user.lastName")
    @Mapping(target = "studyProfile", source = "student.studyProfile")
    StudentWithClassResponse toStudentDetailResponse(Student student, SchoolClassResponse schoolClass,
                                                     TeacherResponse schoolClassTeacher);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studyProfile", source = "studentDetails.studyProfile")
    Student toEntity(User user, StudentDetails studentDetails);

    StudentDetails toStudentDetails(Student student);

    @Mapping(target = "id", source = "student.user.id")
    @Mapping(target = "firstName", source = "student.user.firstName")
    @Mapping(target = "lastName", source = "student.user.lastName")
    @Mapping(target = "username", source = "student.user.username")
    @Mapping(target = "keycloakId", source = "student.user.keycloakId")
    @Mapping(target = "parent", source = "student.parent.user")
    StudentWithParentDto toStudentWithParentDto(Student student);

}

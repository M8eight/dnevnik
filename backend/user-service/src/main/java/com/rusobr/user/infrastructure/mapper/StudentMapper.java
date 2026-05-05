package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentResponseDetail;
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
    @Mapping(target = "schoolClass", source = "schoolClass")
    @Mapping(target = "schoolClassTeacher", source = "schoolClassTeacher")
    StudentResponseDetail toStudentDetailResponse(Student student, SchoolClassResponse schoolClass,
                                                  TeacherResponse  schoolClassTeacher);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studyProfile", source = "studentDetails.studyProfile")
    Student toEntity(User user, StudentDetails studentDetails);

}

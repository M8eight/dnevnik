package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.Attendance;
import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceMapper {
    @Mapping(target = "attendanceId", source = "id")
    AttendanceResponse toAttendanceResponse(Attendance attendance);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonInstance", source = "lessonInstanceEntity")
    Attendance toAttendance(AttendanceRequest attendanceRequest, LessonInstance lessonInstanceEntity);
}

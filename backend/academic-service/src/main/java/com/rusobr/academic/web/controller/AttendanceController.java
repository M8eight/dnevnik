package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.AttendanceService;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PreAuthorize("@teacherSecurity.canCreateAttendance(#attendanceRequest.studentId(), #attendanceRequest.lessonInstanceId(), authentication)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse create(@RequestBody @Valid AttendanceRequest attendanceRequest) {
        return attendanceService.create(attendanceRequest);
    }

    @PreAuthorize("@teacherSecurity.canDeleteAttendance(#id, authentication)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        attendanceService.delete(id);
    }

}

package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.AttendanceService;
import com.rusobr.academic.web.dto.attendances.AttendanceRequest;
import com.rusobr.academic.web.dto.attendances.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public AttendanceResponse createAttendance(@RequestBody AttendanceRequest attendanceRequest) {
        return attendanceService.createAttendance(attendanceRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
    }

}

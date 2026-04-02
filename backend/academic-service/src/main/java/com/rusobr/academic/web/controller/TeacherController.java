package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.TeacherService;
import com.rusobr.academic.web.dto.grade.GradeJournalResponse;
import com.rusobr.academic.web.dto.userService.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class TeacherController {
    private final TeacherService teacherService;

    @GetMapping("/class/{id}")
    public List<UserResponse> getUsersFromClass(@PathVariable Long id) {
        return teacherService.getUsersIdFromClass(id);
    }

    @GetMapping("/class/grades/{teacherAssignmentId}")
    public GradeJournalResponse getClassGrades(@PathVariable Long teacherAssignmentId,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return teacherService.getClassGrades(teacherAssignmentId, date);
    }
}

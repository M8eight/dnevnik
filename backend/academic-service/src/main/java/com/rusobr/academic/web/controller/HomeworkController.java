package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.HomeworkService;
import com.rusobr.academic.web.dto.homework.HomeworkHomePageResponse;
import com.rusobr.academic.web.dto.homework.HomeworkRequest;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/homeworks")
public class HomeworkController {
    private final HomeworkService homeworkService;

    @GetMapping("/by-date")
    public List<HomeworkHomePageResponse> getHomeworksByDate(@RequestParam LocalDate date, @RequestParam Long studentId) {
        return homeworkService.getHomeworksByDate(date, studentId);
    }

    @GetMapping("/by-assignment")
    public Page<HomeworkResponse> getHomeworksByAssignment(@RequestParam Long teachingAssignmentId, Pageable pageable) {
        return homeworkService.getHomeworksByTeachingAssignment(teachingAssignmentId, pageable);
    }

    @PostMapping
    public HomeworkResponse createHomework(@RequestBody HomeworkRequest homeworkRequest) {
        return homeworkService.createHomework(homeworkRequest);
    }
}

package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.HomeworkService;
import com.rusobr.academic.web.dto.homework.HomeworkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/homeworks")
public class HomeworkController {
    private final HomeworkService homeworkService;

    @GetMapping("/by-date")
    public List<HomeworkResponse> getHomeworksByDate(@RequestParam LocalDate date, @RequestParam Long studentId) {
        return  homeworkService.getHomeworksByDate(date, studentId);
    }
}

package com.rusobr.academic.web.controller.bff;

import com.rusobr.academic.application.service.bff.StudentHomeBffService;
import com.rusobr.academic.application.service.bff.StudentInfoBffService;
import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.academic.web.dto.bff.student.HomeAggregation;
import com.rusobr.academic.web.dto.bff.student.StudentInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/bff/students")
@RequiredArgsConstructor
public class StudentBffController {

    private final StudentHomeBffService studentHomeBffService;
    private final StudentInfoBffService studentInfoBffService;
    private final CurrentStudentContext currentStudentContext;

    @GetMapping("/home")
    public HomeAggregation getHomeAggregation(@RequestParam LocalDate date) {
        return studentHomeBffService.getHomeAggregation(date, currentStudentContext.getStudentId());
    }

    @GetMapping("/{id}/info")
    public StudentInfoDto getInfoAggregation(@PathVariable Long id) {
        return studentInfoBffService.getInfoAggregation(id);
    }

}

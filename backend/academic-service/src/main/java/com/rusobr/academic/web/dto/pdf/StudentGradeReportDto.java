package com.rusobr.academic.web.dto.pdf;

import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.common.dto.UserFeignResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record StudentGradeReportDto(
        String title,
        String periodName,
        UserFeignResponse student,
        SchoolClassResponse schoolClass,
        List<StudentGradeReportRow> gradeRows,
        Double totalAverage
) {}

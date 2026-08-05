package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.report.GradeReportService;
import com.rusobr.academic.application.service.report.PdfReportService;
import com.rusobr.academic.web.dto.pdf.StudentGradeReportDto;
import com.rusobr.academic.web.dto.pdf.TeacherGradeReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pdf")
public class PdfExportController {

    private final PdfReportService pdfReportService;
    private final GradeReportService gradeReportService;

    @GetMapping(value = "/student/grade-report/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> studentGradeReport(@AuthenticationPrincipal Jwt jwt, @RequestParam Long periodId) {
        Long studentId = jwt.getClaim("user_id");

        StudentGradeReportDto data = gradeReportService.getStudentGradeReport(studentId, periodId);
        byte[] pdf = pdfReportService.generateStudentGradeReport(data);
        String fileName = "grades-%s-%s.pdf".formatted("27", "4");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/teacher/student-grade-report/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> teacherGradeReport(@RequestParam Long teachingAssignmentId, @RequestParam Long periodId) {
        TeacherGradeReportDto data = gradeReportService.getTeacherGradeReport(teachingAssignmentId, periodId);
        byte[] pdf = pdfReportService.generateTeacherGradeReport(data);
        String fileName = "grades-%s-%s.pdf".formatted("1", "4");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}

package com.rusobr.academic.application.service.report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.rusobr.academic.web.dto.grade.WeightedGrade;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import com.rusobr.academic.web.dto.pdf.*;
import com.rusobr.academic.web.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.rusobr.academic.application.service.JournalService.calculateWeightedAverage;


@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    private final SpringTemplateEngine templateEngine;

    private byte[] generateReport(Context context, String templateName) {
        String html = templateEngine.process(templateName, context);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);

            InputStream regularFont =
                    getClass().getResourceAsStream("/fonts/PTSans-Regular.ttf");
            InputStream boldFont =
                    getClass().getResourceAsStream("/fonts/PTSans-Bold.ttf");
            builder.useFont(
                    () -> regularFont,
                    "PT Sans",
                    400,
                    PdfRendererBuilder.FontStyle.NORMAL,
                    true
            );
            builder.useFont(
                    () -> boldFont,
                    "PT Sans",
                    700,
                    PdfRendererBuilder.FontStyle.NORMAL,
                    true
            );

            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (IOException e) {
            throw new ReportGenerationException("Не удалось сформировать PDF-отчёт");
        }
    }

    public byte[] generateStudentGradeReport(StudentGradeReportDto request) {
        Context context = new Context();
        context.setVariable("title", request.title());
        context.setVariable("periodName", request.periodName());
        context.setVariable("student", request.student());
        context.setVariable("schoolClass", request.schoolClass());
        context.setVariable("rows", request.gradeRows());
        context.setVariable("totalAverage", request.totalAverage());

        return generateReport(context, "grade-report");
    }

    public byte[] generateStudentPeriodFinalGradeReport(StudentPeriodFinalGradeReportDto request) {
        Context context = new Context();
        context.setVariable("title", request.title());
        context.setVariable("academicYearName", request.academicYearName());
        context.setVariable("student", request.student());
        context.setVariable("schoolClass", request.schoolClass());
        context.setVariable("periodFinalGrades", request.periodFinalGrades());

        return generateReport(context, "grade-period-report.html");
    }

    public byte[] generateTeacherGradeReport(TeacherGradeReportDto request) {
        TeacherJournalResponse journal = request.journal();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM");

        List<TeacherReportRow> rows = journal.studentsJournal().stream()
                .map(sj -> new TeacherReportRow(
                        sj.student(),
                        buildGradeChips(sj, journal.lessonInstances(), dateFmt),
                        sj.gradesAverage()
                ))
                .toList();

        List<WeightedGrade> allGrades = journal.studentsJournal().stream()
                .flatMap(s -> s.gradesByLesson().values().stream())
                .flatMap(List::stream)
                .map(g -> (WeightedGrade) g)
                .toList();
        double classAverage = calculateWeightedAverage(allGrades);

        Context context = new Context();
        context.setVariable("title", request.title());
        context.setVariable("teacher", request.teacher());
        context.setVariable("period", journal.academicPeriod());
        context.setVariable("rows", rows);
        context.setVariable("classAverage", classAverage);
        context.setVariable("isDegraded", journal.isDegradedStudents());

        return generateReport(context, "teacher-report");
    }

    private List<GradeChipDto> buildGradeChips(
            StudentJournalDto sj,
            List<LessonInstanceDto> lessons,
            DateTimeFormatter dateFmt) {

        List<GradeChipDto> chips = new ArrayList<>();
        for (LessonInstanceDto lesson : lessons) {
            List<StudentJournalDto.GradeLessonTeacherDto> lessonGrades =
                    sj.gradesByLesson().getOrDefault(lesson.id(), List.of());

            for (var grade : lessonGrades) {
                chips.add(new GradeChipDto(grade.value(), grade.weight(), lesson.lessonDate().format(dateFmt)));
            }
        }
        return chips;
    }

}

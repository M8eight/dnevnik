package com.rusobr.academic.application.service.report;

import com.rusobr.academic.application.service.AcademicYearService;
import com.rusobr.academic.application.service.JournalService;
import com.rusobr.academic.application.service.SchoolClassService;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.academicYear.AcademicYearResponse;
import com.rusobr.academic.web.dto.grade.PeriodFinalGradeResponse;
import com.rusobr.academic.web.dto.lessonInstance.GradeLessonDto;
import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import com.rusobr.academic.web.dto.pdf.StudentGradeReportDto;
import com.rusobr.academic.web.dto.pdf.StudentGradeReportRow;
import com.rusobr.academic.web.dto.pdf.StudentPeriodFinalGradeReportDto;
import com.rusobr.academic.web.dto.pdf.TeacherGradeReportDto;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static com.rusobr.academic.application.service.JournalService.calculateWeightedAverage;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeReportService {

    private final JournalService journalService;
    private final SchoolClassService schoolClassService;
    private final UserClient userClient;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final AcademicYearService academicYearService;

    public StudentGradeReportDto getStudentGradeReport(Long studentId, Long periodId) {
        var student = userClient.getBatchStudents(List.of(studentId)).found().get(0);
        var journal = journalService.getGradesByStudentId(studentId, periodId);
        var schoolClass = schoolClassService.findByStudent(studentId);

        List<StudentGradeReportRow> rows = journal.gradesBySubjects().stream()
                .map(subject -> new StudentGradeReportRow(
                        subject.subject(),
                        subject.grades().stream()
                                .sorted(Comparator.comparing(GradeLessonDto::date))
                                .toList(),
                        subject.average()
                ))
                .toList();

        List<GradeLessonDto> allGrades = journal.gradesBySubjects().stream()
                .flatMap(s -> s.grades().stream())
                .toList();

        double totalAverage = calculateWeightedAverage(allGrades);

        return StudentGradeReportDto.builder()
                .title("Успеваемость за четверть")
                .periodName(journal.academicPeriod().name())
                .student(student)
                .schoolClass(schoolClass)
                .gradeRows(rows)
                .totalAverage(totalAverage).build();
    }

    public StudentPeriodFinalGradeReportDto getStudentPeriodFinalGradeReport(Long studentId, Long academicYearId) {
        AcademicYearResponse academicYear = academicYearService.findById(academicYearId);
        UserFeignResponse student = userClient.getBatchStudents(List.of(studentId)).found().get(0);
        SchoolClassResponse schoolClass = schoolClassService.findByStudent(studentId);
        List<PeriodFinalGradeResponse> periodFinalGrades = journalService.getPeriodFinalGrades(studentId, academicYearId);

        return StudentPeriodFinalGradeReportDto.builder()
                .title("Успеваемость за год")
                .academicYearName(academicYear.name())
                .student(student)
                .schoolClass(schoolClass)
                .periodFinalGrades(periodFinalGrades)
                .build();
    }

    public TeacherGradeReportDto getTeacherGradeReport(Long teachingAssignmentId, Long periodId) {
        TeachingAssignment ta = teachingAssignmentRepository.findById(teachingAssignmentId).orElseThrow(() ->
                new NotFoundException("Teaching assignment with id %s not found".formatted(teachingAssignmentId),
                AcademicExceptionCode.TEACHING_ASSIGNMENT_NOT_FOUND));
        UserFeignResponse teacher = userClient.getTeacherSimpleById(ta.getTeacherId());
        TeacherJournalResponse journal = journalService.getJournalByAssignment(teachingAssignmentId, periodId);

        return TeacherGradeReportDto.builder()
                .title("Табель успеваемости учеников")
                .teacher(teacher)
                .journal(journal).build();
    }

}

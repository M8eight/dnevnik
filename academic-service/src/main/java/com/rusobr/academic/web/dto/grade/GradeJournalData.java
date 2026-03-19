package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodDto;

import java.time.LocalDate;
import java.util.List;

public record GradeJournalData(
   List<LocalDate> dates,
   List<TeacherGradeDto> grades,
   AcademicPeriodDto period
) {}

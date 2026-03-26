package com.rusobr.academic.web.dto.grade;

import com.rusobr.academic.domain.model.ScheduleLesson;
import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodDto;

import java.util.List;

public record GradeJournalData(
   List<DateScheduleAssignDto> dates,
   List<TeacherGradeDto> grades,
   AcademicPeriodDto period,
   List<ScheduleLesson> scheduleLessons
) {}

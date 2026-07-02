package com.rusobr.academic.web.dto.lessonInstance.teacher;

import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;

import java.util.List;

public record TeacherJournalResponse(
        AcademicPeriodResponse academicPeriod,
        BatchUserResponse students,
        List<LessonInstanceDto> lessonInstances,
        List<StudentJournalDto> studentsJournal
) {}

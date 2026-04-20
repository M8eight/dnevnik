package com.rusobr.academic.web.dto.lessonInstance.teacher;

import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import com.rusobr.academic.web.dto.userService.UserResponse;

import java.util.List;

public record TeacherJournalResponse(
        AcademicPeriodResponse academicPeriod,
        List<UserResponse> students,
        List<LessonInstanceDto> lessonInstances,
        List<StudentJournalDto> studentsJournal
) {}

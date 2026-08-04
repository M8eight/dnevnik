package com.rusobr.academic.web.dto.pdf;

import com.rusobr.academic.web.dto.lessonInstance.teacher.StudentJournalDto;
import com.rusobr.common.dto.UserFeignResponse;

import java.util.List;

public record TeacherGradeReportRow(
        UserFeignResponse student,
        List<StudentJournalDto.GradeLessonTeacherDto> grades,
        double average
) {
}

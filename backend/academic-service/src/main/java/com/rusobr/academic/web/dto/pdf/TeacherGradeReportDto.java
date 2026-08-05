package com.rusobr.academic.web.dto.pdf;

import com.rusobr.academic.web.dto.lessonInstance.teacher.TeacherJournalResponse;
import com.rusobr.common.dto.UserFeignResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record TeacherGradeReportDto(
        String title,
        UserFeignResponse teacher,
        TeacherJournalResponse journal
) {
}

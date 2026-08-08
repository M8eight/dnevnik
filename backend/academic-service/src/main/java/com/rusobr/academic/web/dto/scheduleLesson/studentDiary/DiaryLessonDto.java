package com.rusobr.academic.web.dto.scheduleLesson.studentDiary;

import com.rusobr.academic.web.dto.attendances.journal.AttendanceSimpleResponse;
import com.rusobr.academic.web.dto.grade.GradeResponse;
import com.rusobr.academic.web.dto.homework.HomeworkSimpleResponse;
import com.rusobr.academic.web.dto.subject.SubjectResponseDto;

import java.util.List;

public record DiaryLessonDto(
    int lessonNumber,
    SubjectResponseDto subject,
    Long scheduleId,
    Long lessonInstanceId,
    String classRoom,
    List<GradeResponse> grades,
    AttendanceSimpleResponse attendance,
    List<HomeworkSimpleResponse> homeworks
) {
}

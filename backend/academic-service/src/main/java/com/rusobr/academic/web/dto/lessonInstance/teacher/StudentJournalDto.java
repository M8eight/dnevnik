package com.rusobr.academic.web.dto.lessonInstance.teacher;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import com.rusobr.academic.domain.enums.GradeType;
import com.rusobr.academic.web.dto.grade.WeightedGrade;
import com.rusobr.common.dto.UserFeignResponse;

import java.util.List;
import java.util.Map;

public record StudentJournalDto(
        UserFeignResponse student,
        Map<Long, List<GradeLessonTeacherDto>> gradesByLesson,
        Map<Long, AttendanceLessonTeacherDto> attendancesByLesson,
        Double gradesAverage
) {

    public record GradeLessonTeacherDto(
            Long gradeId,
            Integer value,
            Integer weight,
            GradeType gradeType,
            Long studentId,
            Long lessonInstanceId
    ) implements WeightedGrade {
    }

    public record AttendanceLessonTeacherDto(
            Long attendanceId,
            AttendanceStatus status,
            Long studentId,
            Long lessonInstanceId
    ) {
    }

}
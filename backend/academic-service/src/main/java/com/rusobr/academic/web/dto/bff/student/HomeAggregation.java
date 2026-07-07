package com.rusobr.academic.web.dto.bff.student;

import com.rusobr.academic.web.dto.grade.GradeWithSubjectNameResponse;
import com.rusobr.academic.web.dto.homework.HomeworkWithSubjectResponse;
import com.rusobr.academic.web.dto.scheduleLesson.ScheduleLessonResponse;
import com.rusobr.academic.web.dto.scheduleLesson.SchoolLessonResponse;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

public record HomeAggregation(
        List<HomeworkWithSubjectResponse> todayHomework,
        Map<DayOfWeek, List<SchoolLessonResponse>> weekSchedule,
        List<ScheduleLessonResponse> todaySchedule,
        List<GradeWithSubjectNameResponse> todayGrades,
        Double todayAverage
) {}

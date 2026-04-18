package com.rusobr.academic.web.dto.lessonInstance;

import com.rusobr.academic.web.dto.academicPeriod.AcademicPeriodResponse;

import java.time.LocalDate;
import java.util.List;

public record GradesLessonsResponse(
        AcademicPeriodResponse academicPeriod,
        List<LocalDate> dates,
        List<DatesGradesDto> subjects
) {
}

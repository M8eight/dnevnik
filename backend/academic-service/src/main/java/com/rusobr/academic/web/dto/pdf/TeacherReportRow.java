package com.rusobr.academic.web.dto.pdf;

import com.rusobr.common.dto.UserFeignResponse;

import java.util.List;

public record TeacherReportRow(
        UserFeignResponse student,
        List<GradeChipDto> grades,
        Double average
) {}
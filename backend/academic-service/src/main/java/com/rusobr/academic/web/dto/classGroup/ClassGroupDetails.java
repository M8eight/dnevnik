package com.rusobr.academic.web.dto.classGroup;

import com.rusobr.common.dto.BatchUserResponse;

public record ClassGroupDetails(
        Long id,
        String name,
        BatchUserResponse students
) {
}

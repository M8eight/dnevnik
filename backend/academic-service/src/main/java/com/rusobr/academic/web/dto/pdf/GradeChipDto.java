package com.rusobr.academic.web.dto.pdf;

public record GradeChipDto(
        Integer value,
        Integer weight,
        String dateFormatted
) {}
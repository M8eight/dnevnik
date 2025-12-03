package com.rusobr.class_service.web.dto.gradeConstant;

import com.rusobr.class_service.domain.model.GradeConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link GradeConstant}
 */
@Value
@Builder
@Getter
@Setter
public class CreateGradeConstantRequestDto implements Serializable {
    String name;
    String description;
    Integer value;
}
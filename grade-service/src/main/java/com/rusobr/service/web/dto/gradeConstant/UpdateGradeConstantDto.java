package com.rusobr.service.web.dto.gradeConstant;

import com.rusobr.service.domain.model.GradeConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * DTO for {@link GradeConstant}
 */
@Value
@Getter
@Setter
@Builder
public class UpdateGradeConstantDto implements Serializable {
    @Nullable
    String name;
    @Nullable
    String description;
    @Nullable
    Integer value;
}
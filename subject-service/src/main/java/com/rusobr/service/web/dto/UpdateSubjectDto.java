package com.rusobr.class_service.web.dto;

import com.rusobr.class_service.domain.model.Subject;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Subject}
 */
@Value
@Builder
@Getter
@Setter
public class UpdateSubjectDto implements Serializable {
    String name;
    String teacher;
}
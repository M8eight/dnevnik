package com.rusobr.service.web.dto;

import com.rusobr.service.domain.model.Subject;
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
public class CreateSubjectDto implements Serializable {
    String name;
    String teacher;
}
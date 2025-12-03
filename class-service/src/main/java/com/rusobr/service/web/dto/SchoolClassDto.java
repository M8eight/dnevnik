package com.rusobr.service.web.dto;

import com.rusobr.service.domain.model.SchoolClass;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link SchoolClass}
 */
@Value
public class SchoolClassDto implements Serializable {
    String name;
}
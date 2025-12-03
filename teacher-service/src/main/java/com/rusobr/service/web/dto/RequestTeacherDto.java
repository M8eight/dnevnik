package com.rusobr.service.web.dto;

import com.rusobr.service.domain.model.Teacher;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Teacher}
 */
@Value
public class RequestTeacherDto implements Serializable {
    String name;
    String description;
}
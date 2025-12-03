package com.rusobr.service.domain.model;

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
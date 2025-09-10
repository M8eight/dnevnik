package com.rusobr.grade.web.dto.grade;

import com.rusobr.grade.domain.model.Grade;
import lombok.Value;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link Grade}
 */
@Value
public class GradeRequestDto implements Serializable {
    String student;
    String subject;
    String teacher;
    String gradeCode;
    Date date;
}
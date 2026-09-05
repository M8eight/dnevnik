package com.rusobr.common.context;

import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Setter
@Component
@RequestScope
public class CurrentStudentContext {

    private Long studentId;

    public Long getStudentId() {
        if (studentId == null) {
            throw new IllegalStateException("Student context is not resolved for this request");
        }
        return studentId;
    }

}

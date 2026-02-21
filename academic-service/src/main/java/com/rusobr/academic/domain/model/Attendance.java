package com.rusobr.academic.domain.model;

import com.rusobr.academic.domain.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Attendance {
    @Id
    @GeneratedValue
    private Long id;

    private Long studentId;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lesson lessonId;
}

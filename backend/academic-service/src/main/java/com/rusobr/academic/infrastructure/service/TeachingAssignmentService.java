package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeachingAssignmentService {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public TeachingAssignment createOrGet(TeachingAssignmentRequest teachingAssignmentRequest) {
        return teachingAssignmentRepository.findBySubjectIdAndSchoolClassIdAndTeacherId(
                teachingAssignmentRequest.subjectId(),
                teachingAssignmentRequest.classId(),
                teachingAssignmentRequest.teacherId()
        ).orElseGet(() -> {
            TeachingAssignment assignment = TeachingAssignment.builder()
                    .teacherId(teachingAssignmentRequest.teacherId())
                    .schoolClass(schoolClassRepository.getReferenceById(teachingAssignmentRequest.classId()))
                    .subject(subjectRepository.getReferenceById(teachingAssignmentRequest.subjectId()))
                    .build();
            return teachingAssignmentRepository.save(assignment);
        });
    }

    public List<Long> getStudentIdsByTeachingAssignmentId(Long teachingAssignmentId) {
        return teachingAssignmentRepository.findStudentIdsByTeachingAssignmentId(teachingAssignmentId);
    }

}

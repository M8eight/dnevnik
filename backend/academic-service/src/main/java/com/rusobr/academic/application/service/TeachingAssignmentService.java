package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.TeachingAssignmentMapper;
import com.rusobr.academic.domain.model.ClassGroup;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.repository.ClassGroupRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentDetailsDto;
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
    private final TeachingAssignmentMapper teachingAssignmentMapper;
    private final ClassGroupRepository classGroupRepository;

    @Transactional
    public TeachingAssignment createOrGet(TeachingAssignmentRequest taReq) {
        return teachingAssignmentRepository.findBySubjectIdAndSchoolClassIdAndTeacherIdAndClassGroupId(
                taReq.subjectId(),
                taReq.classId(),
                taReq.teacherId(),
                taReq.classGroupId()
        ).orElseGet(() -> {
            TeachingAssignment assignment = TeachingAssignment.builder()
                    .teacherId(taReq.teacherId())
                    .schoolClass(schoolClassRepository.getReferenceById(taReq.classId()))
                    .subject(subjectRepository.getReferenceById(taReq.subjectId()))
                    .classGroup(
                            taReq.classGroupId() != null
                                    ? classGroupRepository.getReferenceById(taReq.classGroupId())
                                    : null
                    )
                    .build();
            return teachingAssignmentRepository.save(assignment);
        });
    }

    public List<Long> getStudentIdsByTeachingAssignmentId(Long teachingAssignmentId) {
        return teachingAssignmentRepository.findStudentIdsByTeachingAssignmentId(teachingAssignmentId);
    }

    public List<TeachingAssignmentDetailsDto> getByTeacherId(Long teacherId) {
        return teachingAssignmentRepository.findTeachingAssignmentDetailByTeacherId(teacherId)
                .stream().map(teachingAssignmentMapper::toTeachingAssignmentDetailsDto).toList();
    }

}

package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.SchoolClassMapper;
import com.rusobr.academic.application.mapper.TeacherSubjectMapper;
import com.rusobr.academic.application.mapper.TeachingAssignmentMapper;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.domain.model.TeacherSubject;
import com.rusobr.academic.domain.model.TeachingAssignment;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeacherSubjectRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.feign.TeacherInfoFeignResponse;
import com.rusobr.academic.web.dto.feign.teacherInfo.TeacherSubjectRawResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.teachingAssignment.TeachingAssignmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherAcademicService {

    private final TeacherSubjectRepository teacherSubjectRepository;
    private final TeacherSubjectMapper teacherSubjectMapper;
    private final SchoolClassRepository schoolClassRepository;
    private final SchoolClassMapper schoolClassMapper;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingAssignmentMapper teachingAssignmentMapper;

    public TeacherInfoFeignResponse getTeacherAcademicInfo(Long teacherId) {
        List<TeacherSubject> teacherSubject = teacherSubjectRepository.findByTeacherId(teacherId);
        List<TeacherSubjectRawResponse> teacherSubjectResponse = teacherSubject.stream()
                .map(teacherSubjectMapper::toRawResponse).toList();

        List<SchoolClass> teacherSchoolClasses = schoolClassRepository.findSchoolClassesByTeacherId(teacherId);
        List<SchoolClassResponse> schoolClassResponse = teacherSchoolClasses.stream()
                .map(schoolClassMapper::toSchoolClassResponse).toList();

        List<TeachingAssignment> teachingAssignments = teachingAssignmentRepository.findByTeacherId(teacherId);
        List<TeachingAssignmentResponse> teachingAssignmentResponses = teachingAssignments.stream()
                .map(teachingAssignmentMapper::toTeachingAssignmentRawResponse).toList();

        return TeacherInfoFeignResponse.builder()
                .subjects(teacherSubjectResponse)
                .classes(schoolClassResponse)
                .assignments(teachingAssignmentResponses)
                .build();

    }

}

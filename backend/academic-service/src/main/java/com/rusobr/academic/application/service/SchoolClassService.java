package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.persistence.projection.SchoolClassProjection;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.application.mapper.SchoolClassMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final SchoolClassMapper schoolClassMapper;
    private final UserClient userClient;

    @Lazy
    @Autowired
    private SchoolClassService self;

    @Transactional(readOnly = true)
    public SchoolClassResponse findById(Long id) {
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SchoolClass Not Found by id: " + id));

        return schoolClassMapper.toSchoolClassResponse(schoolClass);
    }

    @Transactional(readOnly = true)
    public SchoolClassFullResponse findWithClassStudentById(Long id) {
        SchoolClass schoolClass = schoolClassRepository.findWithClassStudentById(id)
                .orElseThrow(() -> new NotFoundException("SchoolClass Not Found by id: " + id));

        List<UserFeignResponse> users = userClient.getBatchUsers(schoolClass.getStudents()
                .stream().map(ClassStudent::getStudentId).toList());

        TeacherResponse teacher = userClient.getTeacherById(schoolClass.getClassTeacherId());

        return schoolClassMapper.toSchoolClassFullResponse(schoolClass, users, teacher);
    }

    @Transactional(readOnly = true)
    public SchoolClassResponse findByStudentId(Long studentId) {
        SchoolClassProjection schoolClass = schoolClassRepository.getSchoolClassByStudentId(studentId)
                .orElseThrow(() -> new NotFoundException("SchoolClass not found for student: " + studentId));
        return schoolClassMapper.toSchoolClassResponse(schoolClass);
    }

    @Transactional(readOnly = true)
    public List<SchoolClassResponse> findAllClasses() {
        return schoolClassRepository.findAllByOrderByNameAsc().stream()
                .map(schoolClassMapper::toSchoolClassResponse).toList();
    }

    public void assignTeacher(Long classId, Long teacherId) {
        userClient.getTeacherById(teacherId);
        self.assignTeacherTransactional(classId, teacherId);
    }

    @Transactional
    public void assignTeacherTransactional(Long classId, Long teacherId) {
        SchoolClass schoolClass = schoolClassRepository.findWithClassStudentById(classId)
                .orElseThrow(() -> new NotFoundException("SchoolClass Not Found by id: " + classId));
        schoolClass.setClassTeacherId(teacherId);
    }

    public SchoolClassResponse create(SchoolClassRequest schoolClassReq) {
        return self.createTransactional(schoolClassReq);
    }

    @Transactional
    public SchoolClassResponse createTransactional(SchoolClassRequest schoolClassReq) {
        if (schoolClassRepository.existsByName(schoolClassReq.name())) {
            throw new ConflictException("School class with name " + schoolClassReq.name() + " already exists");
        }

        SchoolClass schoolClass = schoolClassMapper.toSchoolClass(schoolClassReq);
        return schoolClassMapper.toSchoolClassResponse(schoolClassRepository.save(schoolClass));
    }

    public void update(Long id, SchoolClassRequest schoolClassReq) {
        self.updateTransactional(id, schoolClassReq);
    }

    @Transactional
    public void updateTransactional(Long id, SchoolClassRequest schoolClassReq) {
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SchoolClass Not Found by id: " + id));

        if (schoolClassRepository.existsByNameAndIdNot(schoolClassReq.name(), id)) {
            throw new ConflictException("School class with name " + schoolClassReq.name() + " already exists");
        }

        schoolClassMapper.updateSchoolClass(schoolClass, schoolClassReq);
    }

    @Transactional
    public void delete(Long id) {
        if (!schoolClassRepository.existsById(id)) {
            throw new NotFoundException("SchoolClass not found " + id);
        }
        schoolClassRepository.deleteById(id);
    }

}

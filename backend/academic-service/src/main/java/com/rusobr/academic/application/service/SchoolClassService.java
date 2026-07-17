package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.SchoolClassMapper;
import com.rusobr.academic.domain.model.AcademicYear;
import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.AcademicYearRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.BatchUserResponse;
import com.rusobr.academic.web.dto.feign.TeacherResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassUpdateRequest;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.ExceptionCode;
import com.rusobr.academic.web.exception.NotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final SchoolClassMapper schoolClassMapper;
    private final UserClient userClient;
    private final AcademicYearRepository academicYearRepository;

    @Lazy
    @Autowired
    private SchoolClassService self;

    @Transactional(readOnly = true)
    public SchoolClassResponse findById(Long id) {
        SchoolClass schoolClass = getSchoolClassOrThrow(id);
        return schoolClassMapper.toSchoolClassResponse(schoolClass);
    }

    public SchoolClassFullResponse findWithStudentsById(Long id) {
        SchoolClass schoolClass = self.findWithClassStudentByIdTransactional(id);
        BatchUserResponse users = new BatchUserResponse(List.of(), List.of());
        if (!schoolClass.getStudents().isEmpty()) {
            users = userClient.getBatchUsers(
                    schoolClass.getStudents().stream().map(ClassStudent::getStudentId).toList()
            );
        }

        TeacherResponse teacher = null;
        if (schoolClass.getClassTeacherId() != null) {
            try {
                teacher = userClient.getTeacherById(schoolClass.getClassTeacherId());
            } catch (FeignException.NotFound e) {
                log.warn("Data inconsistency: teacher teacherId={} references non-existent user in user-service",
                        schoolClass.getClassTeacherId());
            }
        }

        return schoolClassMapper.toSchoolClassFullResponse(schoolClass, users, teacher, schoolClass.getClassTeacherId());
    }

    @Transactional(readOnly = true)
    SchoolClass findWithClassStudentByIdTransactional(Long id) {
        return schoolClassRepository.findWithClassStudentById(id)
                .orElseThrow(() -> new NotFoundException("School class with id " + id + " not found",
                        ExceptionCode.SCHOOL_CLASS_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public SchoolClassResponse findByStudent(Long studentId) {
        SchoolClass schoolClass = schoolClassRepository.findSchoolClassByStudentId(studentId)
                .orElseThrow(() -> new NotFoundException("School class with studentId " + studentId + " not found",
                        ExceptionCode.SCHOOL_CLASS_NOT_FOUND));
        return schoolClassMapper.toSchoolClassResponse(schoolClass);
    }

    @Transactional(readOnly = true)
    public List<SchoolClassResponse> findAll() {
        return schoolClassRepository.findAllByOrderByNameAsc().stream()
                .map(schoolClassMapper::toSchoolClassResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SchoolClassResponse> findByAcademicYear(Long academicYearId) {
        return schoolClassRepository.findAllByAcademicYearIdOrderByNameAsc(academicYearId).stream()
                .map(schoolClassMapper::toSchoolClassResponse).toList();
    }

    @Transactional
    public SchoolClassResponse create(SchoolClassRequest schoolClassReq) {
        AcademicYear academicYear = getAcademicYearOrThrow(schoolClassReq.academicYearId());
        validateAcademicYearIsActive(academicYear);

        if (schoolClassRepository.existsByNameAndAcademicYearId(schoolClassReq.name(), schoolClassReq.academicYearId())) {
            throw new ConflictException("School class with name " + schoolClassReq.name() + " already exists in this year"
            , ExceptionCode.SCHOOL_CLASS_UNIQUE_CONFLICT);
        }

        SchoolClass schoolClass = schoolClassMapper.toSchoolClass(schoolClassReq, academicYear);

        return schoolClassMapper.toSchoolClassResponse(schoolClassRepository.save(schoolClass));
    }

    public void assignTeacher(Long classId, Long teacherId) {
        userClient.getTeacherById(teacherId);
        self.assignTeacherDb(classId, teacherId);
    }

    @Transactional
    protected void assignTeacherDb(Long classId, Long teacherId) {
        SchoolClass schoolClass = getSchoolClassWithAcademicYear(classId);

        validateAcademicYearIsActive(schoolClass.getAcademicYear());

        schoolClass.setClassTeacherId(teacherId);
    }

    @Transactional
    public void update(Long id, SchoolClassUpdateRequest request) {
        SchoolClass schoolClass = getSchoolClassWithAcademicYear(id);

        validateAcademicYearIsActive(schoolClass.getAcademicYear());

        schoolClass.setName(request.name());
    }

    @Transactional
    public void delete(Long id) {
        SchoolClass schoolClass = getSchoolClassOrThrow(id);

        validateAcademicYearIsActive(schoolClass.getAcademicYear());

        schoolClassRepository.delete(schoolClass);
    }

    // helpers
    private SchoolClass getSchoolClassOrThrow(Long id) {
        return schoolClassRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("School class with id " + id + " not found",
                        ExceptionCode.SCHOOL_CLASS_NOT_FOUND));
    }

    private SchoolClass getSchoolClassWithAcademicYear(Long id) {
        return schoolClassRepository.findWithAcademicYearById(id)
                .orElseThrow(() -> new NotFoundException("School class with id " + id + " not found",
                        ExceptionCode.SCHOOL_CLASS_NOT_FOUND));
    }

    private AcademicYear getAcademicYearOrThrow(Long academicYearId) {
        return academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("Academic year with id " + academicYearId + " not found",
                        ExceptionCode.ACADEMIC_YEAR_NOT_FOUND));
    }

    private void validateAcademicYearIsActive(AcademicYear academicYear) {
        if (academicYear.isClosed()) {
            throw new ConflictException("Academic year is closed", ExceptionCode.ACADEMIC_YEAR_CLOSED_CONFLICT);
        }
    }
}
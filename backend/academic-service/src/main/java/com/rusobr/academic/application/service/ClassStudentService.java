package com.rusobr.academic.application.service;

import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.ClassStudentRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.exception.ConflictException;
import com.rusobr.academic.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClassStudentService {

    private final SchoolClassRepository schoolClassRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserClient userClient;

    @Lazy
    @Autowired
    private ClassStudentService self;

    public List<UserFeignResponse> getUnassignedStudents() {
        Set<Long> ids = classStudentRepository.findAllStudentIds();
        return userClient.getBatchStudentsExcludeAssigned(ids);
    }

    public void addStudent(Long classId, Long studentId) {
        userClient.existStudentById(studentId);
        self.addStudentTransactional(classId, studentId);
    }

    @Transactional
    public void addStudentTransactional(Long classId, Long studentId) {
        if (!schoolClassRepository.existsById(classId)) {
            throw new NotFoundException("SchoolClass Not Found by id: " + classId);
        }
        if (classStudentRepository.existsByStudentId(studentId)) {
            throw new ConflictException("Student already exists in class");
        }

        SchoolClass schoolClass = schoolClassRepository.getReferenceById(classId);

        classStudentRepository.save(ClassStudent.builder()
                .schoolClass(schoolClass)
                .studentId(studentId)
                .build());
    }

    @Transactional
    public void removeStudent(Long classId, Long studentId) {
        ClassStudent classStudent = classStudentRepository.findBySchoolClassIdAndStudentId(classId, studentId)
                .orElseThrow(() -> new NotFoundException("ClassStudent Not Found by classId: " + classId + " and studentId: " + studentId));

        classStudentRepository.delete(classStudent);
    }

}

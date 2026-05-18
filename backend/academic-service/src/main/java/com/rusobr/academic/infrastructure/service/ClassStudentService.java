package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.ClassStudentRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.web.dto.feign.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassStudentService {

    private final SchoolClassRepository schoolClassRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserClient userClient;

    @Lazy
    @Autowired
    private ClassStudentService self;

    public List<UserResponse> getUnassignedStudents() {
        Set<Long> ids = classStudentRepository.findAllStudentIds();
        return userClient.getBatchUsersExcludeAssigned(ids);
    }

    public void addStudent(Long classId, Long studentId) {
        userClient.getStudentById(studentId);
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

        classStudentRepository.save(ClassStudent.builder()
                .schoolClass(SchoolClass.builder().id(classId).build())
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

package com.rusobr.academic.application.service;

import com.rusobr.academic.application.mapper.ClassGroupMapper;
import com.rusobr.academic.domain.model.ClassGroup;
import com.rusobr.academic.domain.model.ClassGroupStudents;
import com.rusobr.academic.domain.model.ClassStudent;
import com.rusobr.academic.domain.model.SchoolClass;
import com.rusobr.academic.infrastructure.client.UserClient;
import com.rusobr.academic.infrastructure.persistence.repository.ClassGroupRepository;
import com.rusobr.academic.infrastructure.persistence.repository.ClassGroupStudentsRepository;
import com.rusobr.academic.infrastructure.persistence.repository.SchoolClassRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeachingAssignmentRepository;
import com.rusobr.academic.web.dto.classGroup.ClassGroupDetails;
import com.rusobr.academic.web.dto.classGroup.ClassGroupRequest;
import com.rusobr.academic.web.dto.classGroup.ClassGroupResponse;
import com.rusobr.academic.web.dto.classGroup.ClassGroupWithCountResponse;
import com.rusobr.academic.web.exception.AcademicExceptionCode;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.common.exception.ConflictException;
import com.rusobr.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final ClassGroupMapper classGroupMapper;
    private final UserClient userClient;
    private final SchoolClassRepository schoolClassRepository;
    private final ClassGroupStudentsRepository classGroupStudentsRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;

    @Lazy
    @Autowired
    private ClassGroupService self;

    @Transactional
    public List<ClassGroupWithCountResponse> getAllWithCount() {
        return classGroupRepository.findAllWithCountStudents()
                .stream().map(classGroupMapper::toClassGroupResponse).toList();
    }

    @Transactional
    public List<ClassGroupResponse> getAllBySchoolClass(Long schoolClassId) {
        return classGroupRepository.findAllBySchoolClassId(schoolClassId)
                .stream().map(classGroupMapper::toClassGroupResponse).toList();
    }

    public ClassGroupDetails getDetails(Long id) {
        ClassGroup classGroups = classGroupRepository.findWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Class group not found: id=%s".formatted(id),
                        AcademicExceptionCode.CLASS_GROUP_NOT_FOUND));
        List<Long> studentIds = classGroups.getClassGroupStudents().stream()
                .map(ClassGroupStudents::getStudentId).toList();
        BatchUserResponse studentList = userClient.getBatchStudents(studentIds);
        if (studentList.degraded()) {
            log.warn("Failed to load some students for classGroupId={}: notFound={}", id, studentList.notFound());
        }
        return new ClassGroupDetails(id, classGroups.getName(), studentList);
    }

    public List<UserFeignResponse> getUnassignedStudents(Long classGroupId) {
        ClassGroup classGroup = classGroupRepository.findWithSchoolClassById(classGroupId)
                .orElseThrow(() -> new NotFoundException("Class group not found: id=%s".formatted(classGroupId),
                        AcademicExceptionCode.CLASS_GROUP_NOT_FOUND));

        Long schoolClassId = classGroup.getSchoolClass().getId();
        Set<Long> unassignedStudentIds = schoolClassRepository.findUnassignedStudentIdsBySchoolClassId(schoolClassId);
        return userClient.getBatchStudents(unassignedStudentIds.stream().toList()).found();
    }

    @Transactional
    public void create(ClassGroupRequest req) {
        ClassGroup classGroup = ClassGroup.builder()
                .name(req.name())
                .schoolClass(schoolClassRepository.getReferenceById(req.schoolClassId())).build();
        classGroupRepository.save(classGroup);

        log.info("Class group created: id={}, name={}, schoolClassId={}",
                classGroup.getId(), classGroup.getName(), req.schoolClassId());
    }

    @Transactional
    public void delete(Long id) {
        requireClassGroupExists(id);
        if (teachingAssignmentRepository.existsByClassGroupId(id)) {
            throw new ConflictException(
                    "Class group has an active teaching assignment and cannot be deleted: id=%s".formatted(id),
                    AcademicExceptionCode.CLASS_GROUP_ALREADY_ASSIGNMENT);
        }
        classGroupRepository.deleteById(id);

        log.info("Class group deleted: id={}", id);
    }

    @Transactional
    public void update(Long id, String name) {
        ClassGroup classGroup = getClassGroupOrThrow(id);
        classGroup.setName(name);

        log.info("Class group updated: id={}, name={}", id, name);
    }

    public void addStudent(Long classGroupId, Long studentId) {
        requireStudentExistsInUserService(studentId);
        self.addStudentTransactional(classGroupId, studentId);

        log.info("Student added to class group: studentId={}, classGroupId={}", studentId, classGroupId);
    }

    @Transactional
    public void addStudentTransactional(Long classGroupId, Long studentId) {
        ClassGroup classGroup = getClassGroupWithStudentsOrThrow(classGroupId);

        boolean isAlreadyInGroup = classGroup.getClassGroupStudents().stream()
                .anyMatch(cgs -> cgs.getStudentId().equals(studentId));
        if (isAlreadyInGroup) {
            throw new ConflictException(
                    "Student is already a member of this class group: studentId=%s, classGroupId=%s"
                            .formatted(studentId, classGroupId),
                    AcademicExceptionCode.CLASS_GROUP_STUDENT_ALREADY_IN_GROUP);
        }

        boolean isPresentStudent = classGroup.getSchoolClass().getStudents().stream()
                .anyMatch(student -> student.getStudentId().equals(studentId));
        if (!isPresentStudent) {
            throw new ConflictException(
                    "Student does not belong to the school class linked to this group: studentId=%s, classGroupId=%s"
                            .formatted(studentId, classGroupId),
                    AcademicExceptionCode.CLASS_GROUP_STUDENT_NOT_IN_CLASS);
        }

        ClassGroupStudents classGroupStudents = ClassGroupStudents.builder()
                .studentId(studentId).classGroup(classGroup).build();
        classGroup.getClassGroupStudents().add(classGroupStudents);
    }

    public void removeStudent(Long classGroupId, Long studentId) {
        requireStudentExistsInUserService(studentId);
        self.removeStudentTransactional(classGroupId, studentId);

        log.info("Student removed from class group: studentId={}, classGroupId={}", studentId, classGroupId);
    }

    @Transactional
    public void removeStudentTransactional(Long classGroupId, Long studentId) {
        getClassGroupWithStudentsOrThrow(classGroupId);

        ClassGroup classGroup = getClassGroupWithStudentsOrThrow(classGroupId);

        ClassGroupStudents groupStudent = classGroupStudentsRepository
                .findByStudentIdAndClassGroupId(studentId, classGroupId)
                .orElseThrow(() -> new NotFoundException(
                        "Student is not a member of this class group: studentId=%s, classGroupId=%s"
                                .formatted(studentId, classGroupId),
                        AcademicExceptionCode.CLASS_GROUP_STUDENT_NOT_FOUND));

        classGroup.getClassGroupStudents().remove(groupStudent);
        classGroupStudentsRepository.delete(groupStudent);
    }

    // helpers

    private void requireStudentExistsInUserService(Long studentId) {
        if (!userClient.existStudentById(studentId)) {
            throw new NotFoundException("Student not found in user service: id=%s".formatted(studentId),
                    AcademicExceptionCode.USER_SERVICE_STUDENT_NOT_FOUND);
        }
    }

    private void requireClassGroupExists(Long id) {
        if (!classGroupRepository.existsById(id)) {
            throw new NotFoundException("Class group not found: id=%s".formatted(id),
                    AcademicExceptionCode.CLASS_GROUP_NOT_FOUND);
        }
    }

    private ClassGroup getClassGroupOrThrow(Long id) {
        return classGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class group not found: id=%s".formatted(id),
                        AcademicExceptionCode.CLASS_GROUP_NOT_FOUND));
    }

    private ClassGroup getClassGroupWithStudentsOrThrow(Long id) {
        return classGroupRepository.findWithClassGroupAndSchoolClassStudentsById(id)
                .orElseThrow(() -> new NotFoundException("Class group not found: id=%s".formatted(id),
                        AcademicExceptionCode.CLASS_GROUP_NOT_FOUND));
    }

}
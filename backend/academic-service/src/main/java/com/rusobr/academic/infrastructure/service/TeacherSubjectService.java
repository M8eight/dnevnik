package com.rusobr.academic.infrastructure.service;

import com.rusobr.academic.domain.model.Subject;
import com.rusobr.academic.domain.model.TeacherSubject;
import com.rusobr.academic.domain.model.TeacherSubjectId;
import com.rusobr.academic.infrastructure.exception.ConflictException;
import com.rusobr.academic.infrastructure.exception.NotFoundException;
import com.rusobr.academic.infrastructure.feignClient.UserClient;
import com.rusobr.academic.infrastructure.mapper.TeacherSubjectMapper;
import com.rusobr.academic.infrastructure.persistence.repository.SubjectRepository;
import com.rusobr.academic.infrastructure.persistence.repository.TeacherSubjectRepository;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectRequest;
import com.rusobr.academic.web.dto.teacherSubject.TeacherSubjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherSubjectService {

    private final TeacherSubjectRepository teacherSubjectRepository;
    private final TeacherSubjectMapper teacherSubjectMapper;
    private final UserClient userClient;
    private final SubjectRepository subjectRepository;

    public List<TeacherSubjectResponse> findAll() {
        //Получаем учителей из базы academic потом идем в user-service и упаковываем в map, где ключ это id пользователя
        List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findAll();
        List<Long> teacherIds = teacherSubjects.stream().map(ts -> ts.getId().getTeacherId())
                .distinct().toList();
        List<UserFeignResponse> teachers = userClient.getBatchTeachers(teacherIds);
        Map<Long, UserFeignResponse> teachersMap = teachers.stream().collect(Collectors.toMap(
                UserFeignResponse::id,
                teacher -> teacher
        ));

        return teacherSubjects.stream().map(
                teacherSubject -> {
                    UserFeignResponse teacher = teachersMap.get(teacherSubject.getId().getTeacherId());
                    return teacherSubjectMapper.toResponse(teacherSubject, teacher);
                }
        ).toList();
    }

    public TeacherSubjectResponse create(TeacherSubjectRequest request) {
        TeacherSubjectId id = TeacherSubjectId.builder()
                .teacherId(request.teacherId()).subjectId(request.subjectId()).build();

        UserFeignResponse teacher = userClient.getTeacherSimpleById(request.teacherId());

        //Если запись уже существует (safe_delete) восстанавливаем
        Optional<TeacherSubject> teacherSubjectOptional = teacherSubjectRepository
                .findByIdWithDeleted(id.getSubjectId(), id.getTeacherId());
        if (teacherSubjectOptional.isPresent()) {
            TeacherSubject teacherSubject = teacherSubjectOptional.get();
            if (teacherSubject.getDeletedAt() == null)
                throw new ConflictException("Teacher subject relation already exist");

            teacherSubject.setDeletedAt(null);
            return teacherSubjectMapper.toResponse(teacherSubjectRepository.save(teacherSubject), teacher);
        }

        //Иначе создаем новую
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new NotFoundException("Subject not found with id: " + request.subjectId()));

        TeacherSubject teacherSubject = TeacherSubject.builder().id(id).subject(subject).build();
        return teacherSubjectMapper.toResponse(teacherSubjectRepository.save(teacherSubject), teacher);
    }

    @Transactional
    public void delete(TeacherSubjectRequest request) {
        TeacherSubjectId id = TeacherSubjectId.builder()
                .subjectId(request.subjectId()).teacherId(request.teacherId()).build();

        if (!teacherSubjectRepository.existsById(id)) {
            throw new ConflictException("Teacher subject relation not found");
        }

        teacherSubjectRepository.softDelete(id.getSubjectId(), id.getTeacherId());
    }
}

package com.rusobr.user.infrastructure.service.student;

import com.rusobr.user.domain.model.Student;
import com.rusobr.user.domain.model.User;
import com.rusobr.user.infrastructure.exception.NotFoundException;
import com.rusobr.user.infrastructure.feignClient.SchoolClassClient;
import com.rusobr.user.infrastructure.mapper.StudentMapper;
import com.rusobr.user.infrastructure.persistence.repository.StudentRepository;
import com.rusobr.user.infrastructure.persistence.repository.UserRepository;
import com.rusobr.user.infrastructure.service.teacher.TeacherService;
import com.rusobr.user.web.dto.feign.SchoolClassResponse;
import com.rusobr.user.web.dto.student.StudentDetails;
import com.rusobr.user.web.dto.student.StudentResponseDetail;
import com.rusobr.user.web.dto.teacher.TeacherResponse;
import com.rusobr.user.web.dto.user.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;
    private final SchoolClassClient schoolClassClient;
    private final TeacherService teacherService;


    public List<UserResponse> findSimpleBatchStudents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return studentRepository.findAllStudentsByIds(ids);
    }


    public StudentResponseDetail findStudentDetailById(Long id) {
        if (id == null) {
            return null;
        }

        Student student = studentRepository.findWithUserById(id)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        SchoolClassResponse schoolClass = schoolClassClient.getSchoolClassByStudentId(student.getId());
        TeacherResponse teacher = teacherService.findWithUserById(schoolClass.classTeacherId());

        return studentMapper.toStudentDetailResponse(student, schoolClass, teacher);
    }

    public void createStudent(Long userId, StudentDetails studentDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        studentRepository.save(studentMapper.toEntity(user, studentDetails));
    }

//    @Transactional
//    public void deleteStudentById(Long id) {
//        Student student = studentRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Student not found"));
//        studentRepository.delete(student);
//    }

}

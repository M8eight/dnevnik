package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.ClassStudentService;
import com.rusobr.academic.application.service.SchoolClassService;
import com.rusobr.common.context.CurrentStudentContext;
import com.rusobr.common.dto.UserFeignResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/school-classes")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;
    private final ClassStudentService classStudentService;
    private final CurrentStudentContext currentStudentContext;

    @GetMapping("/{id}")
    public SchoolClassResponse getById(@PathVariable Long id){
        return schoolClassService.findById(id);
    }

    @GetMapping("/{id}/details")
    public SchoolClassFullResponse getWithDetailsById(@PathVariable Long id){
        return schoolClassService.findWithStudentsById(id);
    }

    @GetMapping("/by-student")
    public SchoolClassFullResponse getDetailsByStudentId() {
        return schoolClassService.getDetailsByStudentId(currentStudentContext.getStudentId());
    }

    @GetMapping("/search/by-student")
    public SchoolClassResponse getByStudentId(@RequestParam("studentId") @NotNull Long studentId) {
        return schoolClassService.findByStudent(studentId);
    }

    @GetMapping("/by-academic-year/{id}")
    public List<SchoolClassResponse> getByAcademicYear(@PathVariable Long id) {
        return schoolClassService.findByAcademicYear(id);
    }

    @GetMapping
    public List<SchoolClassResponse> findAll() {
        return schoolClassService.findAll();
    }

    @GetMapping("/unassigned-students")
    public List<UserFeignResponse> getAllUnassignedStudents() {
        return classStudentService.getUnassignedStudents();
    }

    @GetMapping("/by-teacher")
    public List<SchoolClassResponse> getByTeacherId(@AuthenticationPrincipal Jwt jwt) {
        Long teacherId = jwt.getClaim("user_id");
        return schoolClassService.getByTeacherId(teacherId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolClassResponse create(@RequestBody @Valid SchoolClassRequest schoolClassReq) {
        return schoolClassService.create(schoolClassReq);
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody @Valid SchoolClassUpdateRequest request) {
        schoolClassService.update(id, request);
    }

    @PostMapping("/{classId}/students/{studentId}")
    public void addStudentToClass(@PathVariable Long classId, @PathVariable Long studentId) {
        classStudentService.addStudent(classId, studentId);
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    public void removeStudentToClass(@PathVariable Long classId, @PathVariable Long studentId) {
        classStudentService.removeStudent(classId, studentId);
    }

    @PutMapping("/{classId}/teacher/{teacherId}")
    public void assignTeacherToClass(@PathVariable Long classId, @PathVariable Long teacherId) {
        schoolClassService.assignTeacher(classId, teacherId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        schoolClassService.delete(id);
    }

}

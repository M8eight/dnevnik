package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.ClassStudentService;
import com.rusobr.academic.infrastructure.service.SchoolClassService;
import com.rusobr.academic.web.dto.feign.UserFeignResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/school-classes")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;
    private final ClassStudentService classStudentService;

    // GET
    @GetMapping("/{id}")
    public SchoolClassResponse getById(@PathVariable Long id){
        return schoolClassService.findById(id);
    }

    @GetMapping("/{id}/details")
    public SchoolClassFullResponse getWithDetailsById(@PathVariable Long id){
        return schoolClassService.findWithClassStudentById(id);
    }

    @GetMapping("/search/by-student")
    public SchoolClassResponse getByStudentId(@RequestParam("studentId") @NotNull Long studentId) {
        return schoolClassService.findByStudentId(studentId);
    }

    @GetMapping
    public List<SchoolClassResponse> findAll() {
        return schoolClassService.findAllClasses();
    }

    @GetMapping("/unassigned")
    public List<UserFeignResponse> getAllUnassignedStudents() {
        return classStudentService.getUnassignedStudents();
    }

    // POST
    @PostMapping
    public SchoolClassResponse create(@RequestBody @Valid SchoolClassRequest schoolClassReq) {
        return schoolClassService.create(schoolClassReq);
    }

    // PATCH
    @PatchMapping("/{id}")
    public void update(@PathVariable @NotNull Long id, @RequestBody SchoolClassRequest schoolClassReq) {
        schoolClassService.update(id, schoolClassReq);
    }

    @PatchMapping("/{classId}/add/{studentId}")
    public void addStudentToClass(@PathVariable Long classId, @PathVariable Long studentId) {
        classStudentService.addStudent(classId, studentId);
    }

    @PatchMapping("/{classId}/remove/{studentId}")
    public void removeStudentToClass(@PathVariable Long classId, @PathVariable Long studentId) {
        classStudentService.removeStudent(classId, studentId);
    }

    @PatchMapping("/{classId}/assign-teacher/{teacherId}")
    public void assignTeacherToClass(@PathVariable Long classId, @PathVariable Long teacherId) {
        schoolClassService.assignTeacher(classId, teacherId);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable @NotNull Long id) {
        schoolClassService.delete(id);
    }

}

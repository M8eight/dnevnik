package com.rusobr.academic.web.controller;

import com.rusobr.academic.infrastructure.service.ClassStudentService;
import com.rusobr.academic.infrastructure.service.SchoolClassService;
import com.rusobr.academic.web.dto.feign.UserResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassFullResponse;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassRequest;
import com.rusobr.academic.web.dto.schoolClass.SchoolClassResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public SchoolClassResponse getSchoolClassById(@PathVariable @NotNull Long id){
        return schoolClassService.findById(id);
    }

    @GetMapping("/{id}/details")
    public SchoolClassFullResponse getSchoolClassWithStudentsById(@PathVariable @NotNull Long id){
        return schoolClassService.findWithClassStudentById(id);
    }

    @GetMapping("/search/by-student")
    public SchoolClassResponse getSchoolClassByStudentId(@RequestParam("studentId") @NotNull Long studentId) {
        return schoolClassService.findClassByStudentId(studentId);
    }

    @GetMapping
    public Page<SchoolClassResponse> findAllClasses(Pageable pageable) {
        return schoolClassService.findAllClasses(pageable);
    }

    @GetMapping("/unassigned")
    public List<UserResponse> getAllUnassignedStudents() {
        return classStudentService.getUnassignedStudents();
    }

    // POST
    @PostMapping
    public SchoolClassResponse saveClass(@RequestBody @Valid SchoolClassRequest schoolClassReq) {
        return schoolClassService.saveClass(schoolClassReq);
    }

    // PATCH
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

    @PatchMapping("/{id}")
    public void update(@PathVariable @NotNull Long id, @RequestBody SchoolClassRequest schoolClassReq) {
        schoolClassService.update(id, schoolClassReq);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteClass(@PathVariable @NotNull Long id) {
        schoolClassService.deleteClass(id);
    }

}

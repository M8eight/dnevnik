package com.rusobr.academic.web.controller;

import com.rusobr.academic.application.service.ClassGroupService;
import com.rusobr.academic.web.dto.classGroup.ClassGroupDetails;
import com.rusobr.academic.web.dto.classGroup.ClassGroupRequest;
import com.rusobr.academic.web.dto.classGroup.ClassGroupResponse;
import com.rusobr.academic.web.dto.classGroup.ClassGroupWithCountResponse;
import com.rusobr.common.dto.BatchUserResponse;
import com.rusobr.common.dto.UserFeignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/class-groups")
@RequiredArgsConstructor
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    @GetMapping
    public List<ClassGroupWithCountResponse> getAll() {
        return classGroupService.getAllWithCount();
    }

    @GetMapping("/by-school-class/{id}")
    public List<ClassGroupResponse> getAllBySchoolClass(@PathVariable Long id) {
        return classGroupService.getAllBySchoolClass(id);
    }

    @GetMapping("/{id}")
    public ClassGroupDetails getDetails(@PathVariable Long id) {
        return classGroupService.getDetails(id);
    }

    @GetMapping("/{classGroupId}/unassigned-students")
    public List<UserFeignResponse> getUnassignedStudents(@PathVariable Long classGroupId) {
        return classGroupService.getUnassignedStudents(classGroupId);
    }

    @PostMapping
    public void create(@RequestBody ClassGroupRequest req) {
        classGroupService.create(req);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        classGroupService.delete(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestParam String name) {
        classGroupService.update(id, name);
    }

    @PostMapping("/{classGroupId}/students/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addStudent(@PathVariable Long classGroupId, @PathVariable Long studentId) {
        classGroupService.addStudent(classGroupId, studentId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}/students/{studentId}")
    public void removeStudent(@PathVariable Long id, @PathVariable Long studentId) {
        classGroupService.removeStudent(id, studentId);
    }

}

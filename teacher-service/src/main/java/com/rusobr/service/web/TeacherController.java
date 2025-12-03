package com.rusobr.service.web;

import com.rusobr.service.domain.model.Teacher;
import com.rusobr.service.domain.service.TeacherService;
import com.rusobr.service.web.dto.RequestTeacherDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public Iterable<Teacher> getAll() {
        return teacherService.getAll();
    }

    @GetMapping("/{id}")
    public Teacher get(@PathVariable Long id) {
        return teacherService.get(id);
    }

    @PostMapping
    public Teacher create(@RequestBody RequestTeacherDto requestTeacherDto) {
        return teacherService.create(requestTeacherDto);
    }

    //TODO update method сделать

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        teacherService.delete(id);
    }

}

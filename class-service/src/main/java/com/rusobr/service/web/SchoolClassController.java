package com.rusobr.service.web;

import com.rusobr.service.domain.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping
    public ResponseEntity<Iterable<Class>> getAllClasses() {
        return ResponseEntity.ok(classService.getAllClasses());
    }

    @PostMapping
    public ResponseEntity<Class> createClass(@RequestBody Class classRequestDto) {
        return ResponseEntity.ok(classService.createClass(classRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Class> updateClass(@PathVariable Long id, @RequestBody Class classRequestDto) {
        return ResponseEntity.ok(classService.updateClass(id, classRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Class> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.ok().build();
    }
}

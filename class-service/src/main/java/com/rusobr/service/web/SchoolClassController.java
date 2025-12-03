package com.rusobr.service.web;

import com.rusobr.service.domain.model.SchoolClass;
import com.rusobr.service.web.dto.SchoolClassDto;
import com.rusobr.service.domain.service.SchoolClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class SchoolClassController {

    private final SchoolClassService classService;

    @GetMapping
    public ResponseEntity<Iterable<SchoolClass>> getAllSchoolClasses() {
        return ResponseEntity.ok(classService.getAllSchoolClasses());
    }

    @PostMapping
    public ResponseEntity<SchoolClass> createSchoolClass(@RequestBody SchoolClassDto classRequestDto) {
        return ResponseEntity.ok(classService.create(classRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolClass> updateSchoolClass(@PathVariable Long id, @RequestBody SchoolClass classRequestDto) {
        return ResponseEntity.ok(classService.update(id, classRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SchoolClass> deleteSchoolClass(@PathVariable Long id) {
        classService.delete(id);
        return ResponseEntity.ok().build();
    }
}

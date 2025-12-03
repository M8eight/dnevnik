package com.rusobr.service.web;

import com.rusobr.service.domain.model.Subject;
import com.rusobr.service.domain.service.SubjectService;
import com.rusobr.service.web.dto.CreateSubjectDto;
import com.rusobr.service.web.dto.UpdateSubjectDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<Iterable<Subject>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@RequestBody CreateSubjectDto createSubjectDto) {
        return ResponseEntity.ok(subjectService.createSubject(createSubjectDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long id, @RequestBody UpdateSubjectDto updateSubjectDto) {
        return ResponseEntity.ok(subjectService.updateSubject(id, updateSubjectDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Subject> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok().build();
    }

}

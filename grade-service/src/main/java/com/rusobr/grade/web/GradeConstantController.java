package com.rusobr.grade.web;


import com.rusobr.grade.domain.model.GradeConstant;
import com.rusobr.grade.domain.service.GradeConstantService;
import com.rusobr.grade.web.dto.gradeConstant.CreateGradeConstantRequestDto;
import com.rusobr.grade.web.dto.gradeConstant.UpdateGradeConstantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeConstantController {

    private final GradeConstantService gradeConstantService;

    @GetMapping
    public ResponseEntity<Iterable<GradeConstant>> getAllGrades() {
        return ResponseEntity.ok(gradeConstantService.getAllGradeConstants());
    }

    @PostMapping
    public ResponseEntity<GradeConstant> createGradeConstant(@RequestBody CreateGradeConstantRequestDto gradeConstantRequestDto) {
        return ResponseEntity.ok(gradeConstantService.createGradeConstant(gradeConstantRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeConstant> updateGradeConstant(@PathVariable Long id, @RequestBody UpdateGradeConstantDto updateGradeConstantDto) {
        return ResponseEntity.ok(gradeConstantService.updateGradeConstant(id, updateGradeConstantDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GradeConstant> deleteGradeConstant(@PathVariable Long id) {
        gradeConstantService.deleteGradeConstant(id);
        return ResponseEntity.ok().build();
    }
}

package com.rusobr.service.domain.service;

import com.rusobr.service.domain.model.GradeConstant;
import com.rusobr.service.infrastructure.persistence.repository.GradeConstantRepository;
import com.rusobr.service.web.dto.gradeConstant.CreateGradeConstantRequestDto;
import com.rusobr.service.web.dto.gradeConstant.UpdateGradeConstantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradeConstantService {

    private final GradeConstantRepository gradeConstantRepository;

    public Iterable<GradeConstant> getAllGradeConstants() {
        return gradeConstantRepository.findAll();
    }

    public GradeConstant createGradeConstant(CreateGradeConstantRequestDto gradeConstant) {
        return gradeConstantRepository.save(GradeConstant.builder()
                .name(gradeConstant.getName())
                .description(gradeConstant.getDescription())
                .value(gradeConstant.getValue())
                .build()
        );
    }

    public GradeConstant updateGradeConstant(Long id, UpdateGradeConstantDto updateGradeConstantDto) {
        GradeConstant gradeConstant = gradeConstantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("GradeConstant not found with id: " + id));

        if (updateGradeConstantDto.getName() != null) {
            gradeConstant.setName(updateGradeConstantDto.getName());
        }
        if (updateGradeConstantDto.getDescription() != null) {
            gradeConstant.setDescription(updateGradeConstantDto.getDescription());
        }
        if (updateGradeConstantDto.getValue() != null) {
            gradeConstant.setValue(updateGradeConstantDto.getValue());
        }

        return gradeConstantRepository.save(gradeConstant);
    }

    public void deleteGradeConstant(Long id) {
        gradeConstantRepository.deleteById(id);
    }

}

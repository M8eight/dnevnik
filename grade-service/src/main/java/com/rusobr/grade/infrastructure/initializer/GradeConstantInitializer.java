package com.rusobr.grade.infrastructure.initializer;

import com.rusobr.grade.domain.model.GradeConstant;
import com.rusobr.grade.infrastructure.persistence.repository.GradeConstantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradeConstantInitializer implements CommandLineRunner {

    private final GradeConstantRepository gradeConstantRepository;

    @Override
    public void run(String... args) throws Exception {
        gradeConstantRepository.save(GradeConstant.builder().name("5").description("Пятерка высшая оценка").value(5).build());
        gradeConstantRepository.save(GradeConstant.builder().name("4").description("Четверка оценка хорошо").value(4).build());
        gradeConstantRepository.save(GradeConstant.builder().name("3").description("Тройка оценка удовлетворительно").value(3).build());
        gradeConstantRepository.save(GradeConstant.builder().name("2").description("Двойка оценка неудовлетворительно").value(2).build());
        gradeConstantRepository.save(GradeConstant.builder().name("1").description("Кол, это печально").value(1).build());
        gradeConstantRepository.save(GradeConstant.builder().name("ОТ").description("Отсутствие по уважительной причине").value(null).build());
        gradeConstantRepository.save(GradeConstant.builder().name("Н").description("Нка, отсутствие по неуважительно причине").value(null).build());
        gradeConstantRepository.save(GradeConstant.builder().name("Б").description("Болен, отсутствие по уважительной причине").value(null).build());
    }
}

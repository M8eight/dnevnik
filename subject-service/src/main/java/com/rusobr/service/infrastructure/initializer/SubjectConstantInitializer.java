package com.rusobr.service.infrastructure.initializer;

import com.rusobr.service.domain.model.Subject;
import com.rusobr.service.infrastructure.persistence.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubjectConstantInitializer implements CommandLineRunner {

    private final SubjectRepository subjectRepository;

    @Override
    public void run(String... args) throws Exception {
        subjectRepository.save(Subject.builder().name("Математика").teacher("John Doe").build());
        subjectRepository.save(Subject.builder().name("Физика").teacher("Jane Doe").build());
        subjectRepository.save(Subject.builder().name("Химия").teacher("John Doe").build());
        subjectRepository.save(Subject.builder().name("Биология").teacher("Jane Doe").build());
        subjectRepository.save(Subject.builder().name("Информатика").teacher("John Doe").build());
        subjectRepository.save(Subject.builder().name("История").teacher("Jane Doe").build());
        subjectRepository.save(Subject.builder().name("Литература").teacher("John Doe").build());
        subjectRepository.save(Subject.builder().name("Философия").teacher("Jane Doe").build());
    }
}

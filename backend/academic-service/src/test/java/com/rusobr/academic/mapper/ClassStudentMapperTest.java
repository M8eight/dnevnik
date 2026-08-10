package com.rusobr.academic.mapper;

import com.rusobr.academic.application.mapper.ClassStudentMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassStudentMapperTest {

    private final ClassStudentMapper mapper = Mappers.getMapper(ClassStudentMapper.class);

    @Test
    void shouldBeInstantiable() {
        assertThat(mapper).isNotNull();
    }
}

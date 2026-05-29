package com.rusobr.academic.infrastructure.mapper;

import com.rusobr.academic.domain.model.LessonInstance;
import com.rusobr.academic.web.dto.lessonInstance.DiaryLessonInstanceDto;
import com.rusobr.academic.web.dto.lessonInstance.LessonInstanceDto;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LessonInstanceMapper {

    LessonInstanceDto toLessonInstanceDto(LessonInstance lessonInstance);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "scheduleId", source = "scheduleLesson.id")
    DiaryLessonInstanceDto toDiaryLessonInstance(LessonInstance lessonInstance);


}

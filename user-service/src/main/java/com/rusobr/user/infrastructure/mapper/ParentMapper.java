package com.rusobr.user.infrastructure.mapper;

import com.rusobr.user.domain.model.Parent;
import com.rusobr.user.web.dto.parent.ParentRequestDto;
import com.rusobr.user.web.dto.parent.ParentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParentMapper {
    ParentResponseDto toParentResponseDto(Parent parent);
    Parent toParent(ParentRequestDto parentRequestDto);
    void updateEntityFromDto(ParentRequestDto dto, @MappingTarget Parent parent);
}

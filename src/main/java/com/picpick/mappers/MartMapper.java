package com.picpick.mappers;

import com.picpick.dtos.MartResponse;
import com.picpick.entities.Mart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MartMapper {
    MartResponse toDto(Mart mart);
}

package com.picpick.mart;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MartMapper {
    MartDto toDto(Mart mart);

    MartMapInfo toMapInfo(Mart mart);

    Mart toEntity(SignupRequest request);
}

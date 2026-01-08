package com.picpick.scan;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScanMapper {
    ScanDto toDto(Scan scan);

    Scan toEntity(ScanDto scanDto);

    Scan toEntity(ScanRequest scanRequest);

    @org.mapstruct.Mapping(source = "id", target = "scanId")
    @org.mapstruct.Mapping(source = "user.id", target = "userId")
    ScanResponse toResponse(Scan scan);
}

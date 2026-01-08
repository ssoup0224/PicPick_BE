package com.picpick.scan;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScanMapper {
    ScanDto toDto(Scan scan);

    @org.mapstruct.Mapping(target = "user", ignore = true)
    @org.mapstruct.Mapping(target = "mart", ignore = true)
    @org.mapstruct.Mapping(target = "gemini", ignore = true)
    Scan toEntity(ScanDto scanDto);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "scannedAt", ignore = true)
    @org.mapstruct.Mapping(target = "naverProductId", ignore = true)
    @org.mapstruct.Mapping(target = "naverBrand", ignore = true)
    @org.mapstruct.Mapping(target = "naverMaker", ignore = true)
    @org.mapstruct.Mapping(target = "naverName", ignore = true)
    @org.mapstruct.Mapping(target = "naverPrice", ignore = true)
    @org.mapstruct.Mapping(target = "naverImage", ignore = true)
    @org.mapstruct.Mapping(target = "isShown", ignore = true)
    @org.mapstruct.Mapping(target = "aiUnitPrice", ignore = true)
    @org.mapstruct.Mapping(target = "user", ignore = true)
    @org.mapstruct.Mapping(target = "mart", ignore = true)
    @org.mapstruct.Mapping(target = "gemini", ignore = true)
    Scan toEntity(ScanRequest scanRequest);

    @org.mapstruct.Mapping(source = "id", target = "scanId")
    @org.mapstruct.Mapping(source = "user.id", target = "userId")
    ScanResponse toResponse(Scan scan);
}

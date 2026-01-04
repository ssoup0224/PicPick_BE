package com.picpick.mappers;

import com.picpick.dtos.ScanLogResponse;
import com.picpick.entities.ScanLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScanLogMapper {

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "martId", source = "mart.id")
    @Mapping(target = "martName", source = "mart.name")
    @Mapping(target = "onlinePrice", source = "onlineItem.itemPrice")
    ScanLogResponse toDto(ScanLog scanLog);
}

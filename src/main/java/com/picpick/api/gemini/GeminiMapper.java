package com.picpick.api.gemini;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GeminiMapper {

    @Mapping(target = "indexes", source = "indexes")
    GeminiResponse toResponse(Gemini gemini);

    GeminiResponse.IndicatorDto toIndicatorDto(Gemini.Indicator indicator);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scan", ignore = true)
    @Mapping(target = "user", ignore = true)
    Gemini toEntity(GeminiResponse response);
}

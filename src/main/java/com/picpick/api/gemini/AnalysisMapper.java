package com.picpick.api.gemini;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scanLog", ignore = true)
    @Mapping(target = "martPrice", ignore = true)
    @Mapping(target = "onlinePrice", ignore = true)
    @Mapping(target = "qualityInfo", source = "qualitySummary")
    @Mapping(target = "priceInfo", source = "priceSummary")
    @Mapping(target = "conclusionInfo", source = "conclusion")
    @Mapping(target = "indexOne", expression = "java(getIndicatorAtIndex(response.getIndices(), 0))")
    @Mapping(target = "indexTwo", expression = "java(getIndicatorAtIndex(response.getIndices(), 1))")
    @Mapping(target = "indexThree", expression = "java(getIndicatorAtIndex(response.getIndices(), 2))")
    @Mapping(target = "indexFour", expression = "java(getIndicatorAtIndex(response.getIndices(), 3))")
    @Mapping(target = "indexFive", expression = "java(getIndicatorAtIndex(response.getIndices(), 4))")
    AnalysisReport toEntity(AnalysisAIResponse response);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scanLog", ignore = true)
    @Mapping(target = "martPrice", ignore = true)
    @Mapping(target = "onlinePrice", ignore = true)
    @Mapping(target = "qualityInfo", source = "qualitySummary")
    @Mapping(target = "priceInfo", source = "priceSummary")
    @Mapping(target = "conclusionInfo", source = "conclusion")
    @Mapping(target = "indexOne", expression = "java(getIndicatorAtIndex(response.getIndices(), 0))")
    @Mapping(target = "indexTwo", expression = "java(getIndicatorAtIndex(response.getIndices(), 1))")
    @Mapping(target = "indexThree", expression = "java(getIndicatorAtIndex(response.getIndices(), 2))")
    @Mapping(target = "indexFour", expression = "java(getIndicatorAtIndex(response.getIndices(), 3))")
    @Mapping(target = "indexFive", expression = "java(getIndicatorAtIndex(response.getIndices(), 4))")
    void updateFromResponse(AnalysisAIResponse response, @MappingTarget AnalysisReport report);

    @Named("getIndicatorAtIndex")
    default String getIndicatorAtIndex(List<AnalysisAIResponse.Indicator> indices, int index) {
        if (indices != null && index < indices.size()) {
            AnalysisAIResponse.Indicator indicator = indices.get(index);
            return indicator.getName() + ": " + indicator.getReason();
        }
        return null;
    }
}

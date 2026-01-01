package com.picpick.api.naver;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverRequestVariableDto {

    private String query;
    private Integer display;
    private Integer start;
    private String sort;
}

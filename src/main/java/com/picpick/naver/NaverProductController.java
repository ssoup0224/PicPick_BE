package com.picpick.naver;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/naver")
public class NaverProductController {

    private final NaverProductService naverProductService;

    @GetMapping("/search")
    public List<NaverProductDto> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "10") Integer display,
            @RequestParam(required = false, defaultValue = "1") Integer start,
            @RequestParam(required = false, defaultValue = "sim") String sort) {
        NaverRequestVariableDto variables = NaverRequestVariableDto.builder()
                .query(query)
                .display(display)
                .start(start)
                .sort(sort)
                .build();

        return naverProductService.naverShopSearchAPI(variables);
    }
}

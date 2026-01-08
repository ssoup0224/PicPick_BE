package com.picpick.api.naver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class NaverService {

    @Value("${NAVER_CLIENT_ID}")
    private String naverClientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String naverClientSecret;

    private final WebClient webClient;

    public NaverService(@Value("${NAVER_URL}") String naverUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(naverUrl)
                .build();
    }

    public Naver searchCheapest(String query) {
        log.info("Searching Naver Shopping for: {}", query);

        try {
            NaverResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search/shop.json")
                            .queryParam("query", query)
                            .queryParam("display", 10)
                            .queryParam("start", 1)
                            .queryParam("sort", "sim")
                            .build())
                    .header("X-Naver-Client-Id", naverClientId)
                    .header("X-Naver-Client-Secret", naverClientSecret)
                    .retrieve()
                    .bodyToMono(NaverResponse.class)
                    .block();

            if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
                return response.getItems().stream()
                        .filter(item -> item.getLprice() != null)
                        .min((i1, i2) -> i1.getLprice().compareTo(i2.getLprice()))
                        .orElse(response.getItems().get(0));
            }
        } catch (Exception e) {
            log.error("Error calling Naver Shopping API: {}", e.getMessage());
        }

        return null;
    }
}

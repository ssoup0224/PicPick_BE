package com.picpick.naver;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NaverProductService {

    @Value("${NAVER_URL}")
    private String naverUrl;

    @Value("${NAVER_CLIENT_ID}")
    private String naverClientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String naverClientSecret;

    public List<NaverProductDto> naverShopSearchAPI(NaverRequestVariableDto naverVariable) {
        // 참고 url: https://ssong915.tistory.com/36

        // 가격순 정렬인 경우, 정확도를 위해 유사도순으로 많이(100개) 가져와서 백엔드에서 필터링 후 정렬한다.
        String searchSort = naverVariable.getSort();
        int searchDisplay = naverVariable.getDisplay() != null ? naverVariable.getDisplay() : 10;
        boolean isPriceSort = "asc".equals(searchSort) || "dsc".equals(searchSort);

        URI uri = UriComponentsBuilder.fromUriString(naverUrl)
                .path("v1/search/shop.json")
                .queryParam("query", naverVariable.getQuery())
                .queryParam("display", isPriceSort ? 100 : searchDisplay)
                .queryParam("start", naverVariable.getStart())
                .queryParam("sort", isPriceSort ? "sim" : searchSort)
                .encode()
                .build()
                .toUri();

        log.info("uri : {}", uri);
        RestTemplate restTemplate = new RestTemplate();

        RequestEntity<Void> req = RequestEntity
                .get(uri)
                .header("X-Naver-Client-Id", naverClientId)
                .header("X-Naver-Client-Secret", naverClientSecret)
                .build();

        ResponseEntity<String> result = restTemplate.exchange(req, String.class);
        List<NaverProductDto> naverProductDtos = fromJSONtoNaverProduct(result.getBody());

        // 백엔드 필터링: 검색어의 모든 토큰이 제목에 포함되어야 함 (HTML 태그 제거 후 비교)
        List<String> queryTokens = Arrays.stream(naverVariable.getQuery().toLowerCase().split(" "))
                .filter(t -> !t.isEmpty())
                .toList();

        List<NaverProductDto> filteredList = naverProductDtos.stream()
                .filter(item -> isHighlyRelevant(item.getTitle(), queryTokens))
                .collect(Collectors.toList());

        // 가격순 정렬인 경우 백엔드에서 다시 정렬
        if (isPriceSort) {
            Comparator<NaverProductDto> priceComparator = Comparator.comparingInt(NaverProductDto::getLprice);
            if ("dsc".equals(searchSort)) {
                priceComparator = priceComparator.reversed();
            }
            filteredList.sort(priceComparator);
        }

        // 요청한 개수만큼 반환
        List<NaverProductDto> finalResults = filteredList.stream()
                .limit(searchDisplay)
                .collect(Collectors.toList());

        log.info("result count: {}, filtered count: {}", naverProductDtos.size(), finalResults.size());
        return finalResults;
    }

    private boolean isHighlyRelevant(String title, List<String> queryTokens) {
        String cleanTitle = title.replaceAll("<[^>]*>", "").toLowerCase();

        // 1. 필수 토큰(용량/무게 등 수치) 필터링 - 반드시 포함되어야 함
        List<String> measurementTokens = queryTokens.stream()
                .filter(t -> normalizeMeasurement(t) != null)
                .toList();

        for (String mToken : measurementTokens) {
            if (!matchesWithUnits(cleanTitle, mToken)) {
                return false;
            }
        }

        // 2. 일반 토큰 필터링 - 유연하게 매칭 (시노님 처리 및 75% 이상 매치)
        List<String> normalTokens = queryTokens.stream()
                .filter(t -> normalizeMeasurement(t) == null)
                .toList();

        if (normalTokens.isEmpty())
            return true;

        long matchCount = normalTokens.stream()
                .filter(token -> containsWithSynonyms(cleanTitle, token))
                .count();

        return (double) matchCount / normalTokens.size() >= 0.75;
    }

    private boolean containsWithSynonyms(String title, String token) {
        if (title.contains(token))
            return true;

        Map<String, List<String>> synonyms = Map.of(
                "피존", List.of("피죤"),
                "피죤", List.of("피존"),
                "리필", List.of("리필용"),
                "용기", List.of("본품"));

        return synonyms.getOrDefault(token, List.of()).stream()
                .anyMatch(title::contains);
    }

    private boolean matchesWithUnits(String title, String token) {
        if (title.contains(token))
            return true;

        String normalizedToken = normalizeMeasurement(token);
        if (normalizedToken == null)
            return false;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)\\s*(l|ml|kg|g)");
        java.util.regex.Matcher matcher = pattern.matcher(title);

        while (matcher.find()) {
            String titleMeasurement = matcher.group(1) + matcher.group(2);
            if (normalizedToken.equals(normalizeMeasurement(titleMeasurement))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeMeasurement(String token) {
        // 숫자 + 단위 추출 regex
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(\\d+\\.?\\d*)(l|ml|kg|g)$");
        java.util.regex.Matcher matcher = pattern.matcher(token.toLowerCase());

        if (matcher.find()) {
            try {
                double value = Double.parseDouble(matcher.group(1));
                String unit = matcher.group(2);

                switch (unit) {
                    case "l":
                        return (int) (value * 1000) + "ml";
                    case "ml":
                        return (int) value + "ml";
                    case "kg":
                        return (int) (value * 1000) + "g";
                    case "g":
                        return (int) value + "g";
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private List<NaverProductDto> fromJSONtoNaverProduct(String result) {
        // 문자열 정보를 JSONObject로 바꾸기
        JSONObject rjson = new JSONObject(result);
        // JSONObject에서 items 배열 꺼내기
        // JSON 배열이기 때문에 보통 배열이랑 다르게 활용해야한다.
        JSONArray naverProducts = rjson.getJSONArray("items");
        List<NaverProductDto> naverProductDtoList = new ArrayList<>();
        for (int i = 0; i < naverProducts.length(); i++) {
            JSONObject naverProductsJson = (JSONObject) naverProducts.get(i);
            NaverProductDto itemDto = new NaverProductDto(naverProductsJson);
            naverProductDtoList.add(itemDto);
        }
        return naverProductDtoList;
    }
}
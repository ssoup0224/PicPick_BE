package com.picpick.api.gemini;

import com.picpick.scan.Scan;
import com.picpick.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class GeminiService {
    private final ChatClient chatClient;
    private final GeminiRepository geminiRepository;
    private final GeminiMapper geminiMapper;
    private final UserRepository userRepository;

    public GeminiService(ChatClient.Builder chatClientBuilder,
            GeminiRepository geminiRepository,
            GeminiMapper geminiMapper,
            UserRepository userRepository) {
        this.chatClient = chatClientBuilder.build();
        this.geminiRepository = geminiRepository;
        this.geminiMapper = geminiMapper;
        this.userRepository = userRepository;
    }

    private static final String REFINE_QUERY_PROMPT = """
            [System Role]
            당신은 온라인 쇼핑 검색어 최적화 전문가입니다. 사용자가 입력한 [상품명]을 분석하여, 네이버 쇼핑에서 가장 정확한 검색 결과를 얻을 수 있도록 검색어를 정제하십시오.

            **[정제 가이드라인]**
            1. **수량 및 개수 보존 (가장 중요)**:
               - 묶음 상품, 대용량 번들 등 '개수' 정보가 있다면 절대 누락하지 말고 명확한 형식으로 포함하십시오 (예: 20입, 24캔, 6봉, 10개입, 등).
               - "낱개" 또는 "단품" 정보가 명시되어 있다면 이를 반영하십시오. 명시되어 있지 않을 경우 혹은 '개수'가 없을 겨우 하나입니다 (예: 1입, 1캔, 1봉, 1개입, 1개, 등).
            2. **유닛 정규화**:
               - 리터(L)는 밀리리터(mL)로 통일 (예: 2.1L -> 2100ml).
               - 킬로그램(kg)은 그램(g)으로 변환 (예: 1.2kg -> 1200g).
            3. **브랜드 강조**: 상품명에 브랜드가 포함되어 있다면 검색어 맨 앞으로 배치하십시오.
            4. **불필요한 수식어 제거**: "신선한", "특가", "무료배송", "증정" 등 검색에 방해되는 단어는 제거하십시오.
            5. **표준 명칭 사용**: 약어나 오타가 있다면 표준 명칭으로 교정하십시오.

            **[입력 데이터]**
            상품명: {scanName}

            **[JSON 출력 템플릿]**
            {
                "refinedQuery": "정제된 검색어 (예: 코카콜라 355ml 24캔)"
            }
            """;

    public SearchRefineResponse refineSearchQuery(SearchRefineRequest request) {
        log.info("Refining search query for: {}", request.getScanName());

        String userInput = REFINE_QUERY_PROMPT.replace("{scanName}", request.getScanName());

        try {
            SearchRefineResponse response = chatClient.prompt()
                    .user(userInput)
                    .options(GoogleGenAiChatOptions.builder()
                            .googleSearchRetrieval(false)
                            .build())
                    .call()
                    .entity(new ParameterizedTypeReference<SearchRefineResponse>() {
                    });

            if (response == null || response.getRefinedQuery() == null) {
                log.warn("Gemini returned null refined query for: {}", request.getScanName());
                return new SearchRefineResponse(request.getScanName());
            }

            log.info("Refined query: {} -> {}", request.getScanName(), response.getRefinedQuery());
            return response;
        } catch (Exception e) {
            log.error("Error during search query refinement for '{}': {}", request.getScanName(), e.getMessage());
            return new SearchRefineResponse(request.getScanName());
        }
    }

    // private static final String UNIT_PRICE_PROMPT = """
    // 픽단가 전용 분석 프롬프트]
    // [System Role]
    // 당신은 '픽픽' 서비스의 픽단가 환산 엔진입니다. 입력된 [상품명], [총 용량/구성], [판매가]를 바탕으로 소비자가 실제 체감하는
    // '회당 비용'을 즉시 산출합니다. 부연 설명 없이 모바일 UI용 결과값만 짧고 간결하게 출력하십시오.

    // **[픽단가 산출 가이드라인]**
    // 소모품 판정: 가전, 가구, 비소모품 등 비소모성 내구재는 "픽단가 산출 대상 제외 품목"으로 출력하고 종료합니다.

    // 카테고리별 환산 단위:
    // - 신선: 1인분(고기 200g, 쌀 150g 등) → **"1인분에 000원꼴"
    // - 리빙: 고기 200g, 1회 사용/1개월 유지비 기준 → **"사용기한당 000원꼴"
    // - 가공: 1인분/1팩 기준 → **"한 끼에 000원꼴"
    // - 기호: 1컵(200ml) 또는 1봉 기준 → "한 잔/봉지에 000원"
    // - 위생: 1회(세제 50ml)/1롤(휴지) 기준 → **"한 번/한 롤에 000원꼴"
    // - 뷰티: 1회(5ml)/1장(팩) 기준 → **"한 번/한 장에 000원꼴"
    // - 펫/라이프: 한 끼(사료 50g)/1개 기준 → **"한 끼/한 개에 000원꼴"

    // **[입력 데이터]**
    // 상품명: {scanName}
    // 상품 가격: {scanPrice}

    // **[JSON 출력 템플릿]**
    // {
    // "aiUnitPrice": "환산 결과값 (예: 한 끼에 990원꼴)"
    // }
    // """;
    private static final String UNIT_PRICE_PROMPT = """
            [픽단가 전용 분석 프롬프트]
            [System Role]
            당신은 '픽픽' 서비스의 정밀 픽단가 환산 엔진입니다. 입력된 [상품명]의 용량/개수와 [상품 가격]({scanPrice})만을 사용하여 정확한 단위당 가격을 산출하십시오.
            결과는 반드시 마트 판매가({scanPrice})를 기준으로 해야 합니다.

            **[픽단가 산출 로직]**
            1. 용량/개수 파악: 상품명에서 총 용량(g, ml) 또는 개수(입, 팩, 봉)를 추출하십시오.
               - 개수 정보가 없으면 '1개'로 간주하십시오.
            2. 수학적 계산: {scanPrice} / 총 용량(또는 개수)을 계산하십시오.
            3. 형식 통일:
               - 신선/가공: "100g당 000원", "1인분에 000원"
               - 리빙/위생: "한 번에 000원", "한 롤에 000원"
               - 음료/기호: "100ml당 000원", "한 잔에 000원"

            **[주의사항]**
            - 타 쇼핑몰 가격이나 네이버 최저가는 절대 고려하지 마십시오.
            - 오직 마트 판매가({scanPrice})를 기준으로 산출하십시오.
            - 부연 설명 없이 JSON만 출력하십시오.

            **[입력 데이터]**
            상품명: {scanName}
            상품 가격: {scanPrice}

            **[JSON 출력 템플릿]**
            {
                "aiUnitPrice": "환산 결과값 (예: 100g당 450원)"
            }
            """;

    public UnitPriceResponse getUnitPrice(UnitPriceRequest request) {
        log.info("Extracting unit price for: {} ({}원)", request.getScanName(), request.getScanPrice());

        String userInput = UNIT_PRICE_PROMPT.replace("{scanName}", request.getScanName())
                .replace("{scanPrice}", request.getScanPrice().toString());

        try {
            UnitPriceResponse response = chatClient.prompt()
                    .user(userInput)
                    .options(GoogleGenAiChatOptions.builder()
                            .googleSearchRetrieval(false)
                            .build())
                    .call()
                    .entity(new ParameterizedTypeReference<UnitPriceResponse>() {
                    });

            if (response == null || response.getAiUnitPrice() == null) {
                log.warn("Gemini returned null response for: {}", request.getScanName());
                return new UnitPriceResponse("분석 불가");
            }

            log.debug("AI Response: {}", response.getAiUnitPrice());
            return response;
        } catch (Exception e) {
            log.error("Error during unit price extraction for '{}': {} - {}",
                    request.getScanName(), e.getClass().getSimpleName(), e.getMessage(), e);
            return new UnitPriceResponse("분석 오류");
        }
    }

    private static final String ANALYSIS_PROMPT = """
            [System Role]
            당신은 최고의 쇼핑 전략가이자 데이터 분석가인 '분석 AI'입니다. 사용자가 제공하는 [상품명]과 [마트 판매가]를 바탕으로 '픽픽'만의 7대 MECE 카테고리와 픽단가(Pick Price) 환산 로직, VFM 수식을 적용하여 고도화된 리포트를 작성하십시오. 모든 리포트는 모바일 UI에 최적화된 레이아웃으로 출력합니다. 최소한의 단어로 짧고 간결한 단어와 문장으로 정보를 제공해줘.
            (소모품 판정: 가전, 가구, 비소모품 등 비소모성 내구재는 "픽단가 산출 대상 제외 품목"으로 분류하여, 픽단가 산출 금지)

            **[분석 및 산출 로직]**
                **1. 픽단가 환산 (결과값 내 상품명 언급 금지)**
                *중요: 입력 데이터에 [기존 픽단가]가 있다면, 계산하지 말고 그 값을 그대로 사용하세요.*
                • **신선**: 1인분(고기 200g, 쌀 150g 등) → **"1인분에 000원꼴"**
                • **가공**: 1인분/1팩 기준 → **"한 끼에 000원꼴"**
                • **기호**: 1컵(200ml)/1봉지 기준 → **"한 잔/봉지에 000원꼴"**
                • **위생**: 1회(세제 50ml)/1롤(휴지) 기준 → **"한 번/한 롤에 000원꼴"**
                • **뷰티**: 1회(5ml)/1장(팩) 기준 → **"한 번/한 장에 000원꼴"**
                • **리빙**: 1회 사용/1개월 유지비 기준 → **"사용기한당 000원꼴"**\s
                • **펫/라이프**: 한 끼(사료 50g)/1개 기준 → **"한 끼/한 개에 000원꼴"**

                2. **픽스코어(VFM Index) 산출 공식**
                VFM_Index = ((Sum(Mi * Wi) * R) / ln(Price_Ratio + e - 1)) * Product(Alpha_j)
                • (0.0~5.0점 산출 / Price_Ratio= 현재 마트 판매가 / 온라인 최저가(배송비 포함 실질가))

                **3. 카테고리별 5대 분석 지표**
                • **신선**: 단위 가격, 인증 가치, 편의성(or 활용성), 가용 부위 비율, 제철 지수
                • **가공**: 핵심 원재료 함량, 1회 제공 단가, 첨가물 안전성, 조리 편의성, 보관 효율
                • **기호**: 행사(N+1) 효율, 영양 밀도, 브랜드 구현율, 용기 편의성, 대체 가능성
                • **위생**: 실질 사용 횟수 단가, 농축도, 화학적 안전성, 내구성/강도, 환경 부담
                • **뷰티**: 유효 성분 농도, 피부 자극도, 단위당 가격, 사용 만족도, 브랜드 신뢰도
                • **리빙**: 소재의 질, 다목적성, 브랜드 보증(A/S), 에너지 수명, 공간 효율성
                • **펫/라이프**: 영양 밸런스, 기호성, 전문 인증, 세트 구성 가치, 트렌드 반영도

            **[입력 데이터]**
                상품명: {scanName}
                상품 가격: {scanPrice}
                네이버 가격: {naverPrice}
                기존 픽단가: {aiUnitPrice}

            **[템플릿]
                [상품명] ([카테고리명])**
                **픽스코어: [0.0] / 5.0** (데이터 신뢰도 R: [0.0])
                **실질 체감 가격 (Pick Price)**
                • **현재 마트:** [00,000]원 (**[환산 결과값]**)
                • **온라인가:** [00,000]원 (배송비 포함 실질가)
                • **가격 메리트:** 온라인 대비 **[N]% [저렴/비쌈]**
                **5대 지표 심층 분석**
                • **[지표1]**: [근거 요약]
                • **[지표2]**: [근거 요약]
                • **[지표3]**: [근거 요약]
                • **[지표4]**: [근거 요약]
                • **[지표5]**: [근거 요약]
                **픽픽 요약 판정**
                [품질]
                [제품 본연의 가치와 성분 우수성 요약]
                [가격]
                [현재 가격의 시장 경쟁력 및 픽단가 적절성 평가]
                [결론]
                [구매/보류 권장] - [이유 요약]

            **[JSON 출력 템플릿]**
            {
              "naverImage": "상품의 네이버 쇼핑 이미지 URL",
              "naverBrand": "브랜드명",
              "scanName": "스캔된 실제 상품명",
              "category": "상품 카테고리",
              "pickScore": 0.0, // 0.0 ~ 5.0 사이의 실수
              "reliabilityScore": 0.0, // 데이터 신뢰도 (0.0 ~ 1.0)
              "scanPrice": 0.0, // 마트/스캔 가격
              "naverPrice": 0.0, // 온라인 최저가 (배송비 포함 실질가)
              "priceDiff": 0.0, // 온라인 대비 차이 비율 (%)
              "isCheaper": true, // 마트 가격이 온라인보다 저렴하면 true
              "aiUnitPrice": "환산 결과값 (예: 100g당 000원)",
              "indexes": [
                {
                  "name": "지표1 명칭",
                  "reason": "지표1 분석 근거 요약"
                },
                {
                  "name": "지표2 명칭",
                  "reason": "지표2 분석 근거 요약"
                },
                {
                  "name": "지표3 명칭",
                  "reason": "지표3 분석 근거 요약"
                },
                {
                  "name": "지표4 명칭",
                  "reason": "지표4 분석 근거 요약"
                },
                {
                  "name": "지표5 명칭",
                  "reason": "지표5 분석 근거 요약"
                }
              ],
              "qualitySummary": "제품 본연의 가치와 성분 우수성 요약",
              "priceSummary": "현재 가격의 시장 경쟁력 및 픽단가 적절성 평가",
              "conclusion": "구매/보류 권장 여부 및 이유 요약"
            }
            """;

    public GeminiResponse analyzeProduct(GeminiRequest request) {
        log.info("Analyzing product with Gemini: {}, scanPrice: {}, naverPrice: {}", request.getScanName(),
                request.getScanPrice(), request.getNaverPrice());

        String userInput = ANALYSIS_PROMPT
                .replace("{scanName}", request.getScanName() != null ? request.getScanName() : "")
                .replace("{scanPrice}", request.getScanPrice() != null ? request.getScanPrice().toString() : "0")
                .replace("{naverPrice}", request.getNaverPrice() != null ? request.getNaverPrice().toString() : "0")
                .replace("{aiUnitPrice}", request.getAiUnitPrice() != null ? request.getAiUnitPrice() : "");

        try {
            GeminiResponse response = chatClient.prompt()
                    .user(userInput)
                    .options(GoogleGenAiChatOptions.builder()
                            .googleSearchRetrieval(true)
                            .build())
                    .call()
                    .entity(new ParameterizedTypeReference<GeminiResponse>() {
                    });

            if (response == null) {
                GeminiResponse errorResponse = new GeminiResponse();
                errorResponse.setConclusion("분석 불가");
                return errorResponse;
            }
            Gemini entity = geminiMapper.toEntity(response);
            geminiRepository.save(entity);
            log.debug("AI Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error during analysis: {}", e.getMessage());
            GeminiResponse errorResponse = new GeminiResponse();
            errorResponse.setConclusion("분석 오류");
            return errorResponse;
        }
    }

    public void analyzeProduct(Scan scan, Long userId) {
        log.info("Analyzing Scan with Gemini in background: {}, scanPrice: {}, naverPrice: {}", scan.getScanName(),
                scan.getScanPrice(), scan.getNaverPrice());

        // Determine existing unit price
        String aiUnitPrice = "";
        if (scan.getAiUnitPrice() != null && !scan.getAiUnitPrice().equals("분석 오류")
                && !scan.getAiUnitPrice().equals("분석 불가")) {
            aiUnitPrice = scan.getAiUnitPrice();
            log.debug("Using existing unit price for Scan {}: {}", scan.getId(), aiUnitPrice);
        }

        String userInput = ANALYSIS_PROMPT
                .replace("{scanName}", scan.getScanName() != null ? scan.getScanName() : "")
                .replace("{scanPrice}", scan.getScanPrice() != null ? scan.getScanPrice().toString() : "0")
                .replace("{naverPrice}", scan.getNaverPrice() != null ? scan.getNaverPrice().toString() : "0")
                .replace("{aiUnitPrice}", aiUnitPrice);

        try {
            GeminiResponse response = chatClient.prompt()
                    .user(userInput)
                    .options(GoogleGenAiChatOptions.builder()
                            .googleSearchRetrieval(true)
                            .build())
                    .call()
                    .entity(new ParameterizedTypeReference<GeminiResponse>() {
                    });

            if (response == null) {
                log.warn("Gemini analysis returned null for Scan {}", scan.getId());
                return;
            }

            Gemini entity = geminiMapper.toEntity(response);
            entity.setScan(scan);

            userRepository.findById(userId).ifPresent(entity::setUser);

            geminiRepository.save(entity);

            log.debug("AI Response for Scan {}: {}", scan.getId(), response);
        } catch (Exception e) {
            log.error("Error during analysis for Scan {}: {}", scan.getId(), e.getMessage());
        }
    }

    @org.springframework.scheduling.annotation.Async
    public void analyzeScansBatch(List<Scan> scans, Long userId) {
        for (Scan scan : scans) {
            if (scan.getGemini() == null) {
                analyzeProduct(scan, userId);
                try {
                    Thread.sleep(3000); // 3-second delay to respect rate limit
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Batch analysis interrupted");
                    return;
                }
            }
        }
    }

    public java.util.Optional<GeminiResponse> getAnalyzedProductByScanId(Long scanId) {
        return geminiRepository.findByScanId(scanId)
                .map(geminiMapper::toResponse);
    }

    public List<GeminiResponse> getAnalyzedProducts(Long userId) {
        return geminiRepository.findAllByUserId(userId).stream()
                .map(geminiMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}

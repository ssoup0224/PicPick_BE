package com.picpick.api.gemini;

import com.picpick.scan.Scan;
import com.picpick.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String UNIT_PRICE_PROMPT = """
            픽단가 전용 분석 프롬프트]
            [System Role]
            당신은 '픽픽' 서비스의 픽단가 환산 엔진입니다. 입력된 [상품명], [총 용량/구성], [판매가]를 바탕으로 소비자가 실제 체감하는 '회당 비용'을 즉시 산출합니다. 부연 설명 없이 모바일 UI용 결과값만 짧고 간결하게 출력하십시오.

            **[픽단가 산출 가이드라인]**
            소모품 판정: 가전, 가구, 비소모품 등 비소모성 내구재는 "픽단가 산출 대상 제외 품목"으로 출력하고 종료합니다.

            카테고리별 환산 단위:
            - 신선: 1인분(고기 200g, 쌀 150g 등) → **"1인분에 000원꼴"
            - 리빙: 고기 200g, 1회 사용/1개월 유지비 기준 → **"사용기한당 000원꼴"
            - 가공: 1인분/1팩 기준 → **"한 끼에 000원꼴"
            - 기호: 1컵(200ml) 또는 1봉 기준 → "한 잔/봉지에 000원"
            - 위생: 1회(세제 50ml)/1롤(휴지) 기준 → **"한 번/한 롤에 000원꼴"
            - 뷰티: 1회(5ml)/1장(팩) 기준 → **"한 번/한 장에 000원꼴"
            - 펫/라이프: 한 끼(사료 50g)/1개 기준 → **"한 끼/한 개에 000원꼴"

            **[입력 데이터]**
            상품명: {scanName}
            상품 가격: {scanPrice}

            **[JSON 출력 템플릿]**
            {
                "aiUnitPrice": "환산 결과값 (예: 한 끼에 990원꼴)"
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

    @Transactional
    public void analyzeProduct(Scan scan, Long userId) {
        log.info("Analyzing Scan with Gemini in background: {}, scanPrice: {}, naverPrice: {}", scan.getScanName(),
                scan.getScanPrice(), scan.getNaverPrice());

        // Determine existing unit price
        String aiUnitPrice = "";
        if (scan.getAiUnitPrice() != null && !scan.getAiUnitPrice().equals("분석 오류")
                && !scan.getAiUnitPrice().equals("분석 불가")) { // Skip placeholder
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

    @Transactional
    public Optional<Gemini> findAndCloneAnalysis(Scan scan, Long userId) {
        return geminiRepository.findFirstByScanNameOrderByIdDesc(scan.getScanName())
                .map(existingGemini -> {
                    log.info("Reusing existing Gemini analysis for: {}", scan.getScanName());
                    Gemini newGemini = copyGemini(existingGemini);
                    newGemini.setScan(scan);
                    scan.setGemini(newGemini); // Maintain bidirectional consistency
                    userRepository.findById(userId).ifPresent(newGemini::setUser);
                    return geminiRepository.save(newGemini);
                });
    }

    private Gemini copyGemini(Gemini source) {
        Gemini target = Gemini.builder()
                .naverImage(source.getNaverImage())
                .naverBrand(source.getNaverBrand())
                .scanName(source.getScanName())
                .category(source.getCategory())
                .pickScore(source.getPickScore())
                .reliabilityScore(source.getReliabilityScore())
                .scanPrice(source.getScanPrice())
                .naverPrice(source.getNaverPrice())
                .priceDiff(source.getPriceDiff())
                .isCheaper(source.getIsCheaper())
                .aiUnitPrice(source.getAiUnitPrice())
                .qualitySummary(source.getQualitySummary())
                .priceSummary(source.getPriceSummary())
                .conclusion(source.getConclusion())
                .build();

        if (source.getIndexes() != null) {
            List<Gemini.Indicator> newIndexes = source.getIndexes().stream()
                    .map(i -> new Gemini.Indicator(i.getName(), i.getReason()))
                    .collect(Collectors.toList());
            target.setIndexes(newIndexes);
        }

        return target;
    }

    @Async
    public void analyzeScansBatch(List<Scan> scans, Long userId) {
        log.info("Starting batch analysis for {} items", scans.size());
        for (Scan scan : scans) {
            if (scan.getGemini() == null) {
                analyzeProduct(scan, userId);
                log.info("Finished analysis of product: {}", scan.getScanName());
                try {
                    Thread.sleep(3000); // 3-second delay to respect rate-limit for background calls
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Batch analysis interrupted");
                    return;
                }
            }
        }
    }

    public Optional<GeminiResponse> getAnalyzedProductByScanId(Long scanId) {
        return geminiRepository.findByScanId(scanId)
                .map(geminiMapper::toResponse);
    }

    public List<GeminiResponse> getAnalyzedProducts(Long userId) {
        return geminiRepository.findAllByUserId(userId).stream()
                .map(geminiMapper::toResponse)
                .collect(Collectors.toList());
    }
}
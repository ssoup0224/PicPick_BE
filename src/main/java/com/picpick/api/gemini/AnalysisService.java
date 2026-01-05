package com.picpick.api.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picpick.repositories.AnalysisReportRepository;
import com.picpick.repositories.ScanLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisService {
    private final ChatClient.Builder chatClientBuilder;
    private final AnalysisReportRepository analysisReportRepository;
    private final ScanLogRepository scanLogRepository;
    private final AnalysisMapper analysisMapper;
    private final ObjectMapper objectMapper;

    private ChatClient chatClient;

    private static final String ANALYSIS_PROMPT = """
            [System Role]
            당신은 최고의 쇼핑 전략가이자 데이터 분석가인 '분석 AI'입니다. 사용자가 제공하는 {productName}, {martPrice}, {onlinePrice}를 바탕으로 '픽픽'만의 7대 MECE 카테고리와 픽단가(Pick Price) 환산 로직, VFM 수식을 적용하여 고도화된 리포트를 작성하십시오. 모든 리포트는 분석하여 JSON 형식으로 작성하십시오.

            **[반드시 지켜야 할 출력 형식]**
            1. 반드시 아래의 JSON 스키마를 따르는 순수 JSON 데이터만 출력하십시오.
            2. Markdown 코드 블록(```json ... ```)을 사용하지 말고 raw JSON으로 출력하십시오.
            3. 모든 필드는 필수이며, null이 될 수 없습니다.

            **[JSON 스키마]**
            {
              "productName": "{productName}",
              "chosenCategory": "판단된 카테고리명",
              "pickScore": 0.0,
              "credibilityScore": 0.0,
              "pickPriceInfo": "환산 결과값 (예: 한 끼에 990원꼴)",
              "priceDifferencePercent": 0.0,
              "isCheaperThanOnline": true,
              "indices": [
                { "name": "지표1", "reason": "근거 요약" },
                { "name": "지표2", "reason": "근거 요약" },
                { "name": "지표3", "reason": "근거 요약" },
                { "name": "지표4", "reason": "근거 요약" },
                { "name": "지표5", "reason": "근거 요약" }
              ],
              "qualitySummary": "제품 본연의 가치와 성분 우수성 요약",
              "priceSummary": "현재 가격의 시장 경쟁력 및 픽단가 적절성 평가",
              "conclusion": "구매/보류 권장 사유 요약"
            }

            **[분석 및 산출 로직]**
            1. 카테고리 판단 및 픽단가 환산
            *제공된 상품명을 바탕으로 아래 7개 카테고리 중 하나를 선택하십시오:*
            - 신선 식품: 1인분(고기 200g, 쌀 150g 등) -> "1인분에 000원꼴"
            - 가공 식료품: 1인분/1팩 기준 -> "한 끼에 000원꼴"
            - 기호/음료: 1컵(200ml)/1봉지 기준 -> "한 잔/봉지에 000원꼴"
            - 생활 위생: 1회(세제 50ml)/1롤(휴지) 기준 -> "한 번/한 롤에 000원꼴"
            - 퍼스널 케어: 1회(5ml)/1장(팩) 기준 -> "한 번/한 장에 000원꼴"
            - 홈 리빙: 1회 사용/1개월 유지비 기준 -> "시간당 000원꼴"
            - 펫/라이프: 한 끼(사료 50g)/1개 기준 -> "한 끼/한 개에 000원꼴"

            2. 픽스코어(VFM Index) 산출 공식
            VFM_Index = ((Sum(Mi * Wi) * R) / ln(Price_Ratio + e - 1)) * Product(Alpha_j)
            - (0.0~5.0점 산출 / Price_Ratio = 현재 마트 판매가 / 온라인 최저가)
            
            2.1 일반 텍스트 버전
            - 가성비 지수(VFM Index) = [ {Σ(지표 점수 × 가중치) × 신뢰도 계수} / ln(가격 비율 + e - 1) ] × 보정 계수 총곱
                = 가성비 지수(VFM Index) = [ {Σ(Mi × Wi) × R} / ln(Pratio + e - 1) ] × alpha
                
            2.2 상세 변수 정의
            - VFM_Index: 최종 가성비 점수 (0~5.0)
            - Sum(Mi * Wi): 5대 지표 점수(1~10점)에 카테고리별 가중치를 곱한 합산 가치
            - R (Reliability/Credibility): 데이터 신뢰도 계수 (0~1.0 사이, 정보가 확실할수록 1에 수렴)
            - ln(Price_Ratio + e - 1): 가격 비율에 따른 점수 하락 곡선 (자연로그 함수를 사용하여 가격이 비싸질수록 가성비 점수가 완만하게 깎이도록 설계)
                 -Price_Ratio = 마트 판매가 / 시장 적정가
            - Product(Alpha_j): 행사(1+1 등), 유통기한, PB 상품 여부에 따른 가산점들의 전체 곱
            
            2.3 주요 변수 설명
            - 가치 점수 (Mi, Wi): AI가 판정한 5대 지표 점수(Mi)와 카테고리별 가중치(Wi)의 합입니다.
            - 가격 효율성 (Pratio): 현재 판매가 / 시장 적정가 (온라인 최저가와 오프라인 평균가의 복합 기준)입니다. 로그 함수를 적용해 가격 변동에 따른 점수 왜곡을 방지합니다.
            - 신뢰도 계수 (R): 데이터의 최신성, 리뷰 수 등을 반영하여 점수의 확실성을 보정합니다.
            - 보정 계수 (alpha): 1+1 행사(alpha_promo), PB 상품(alpha_PB) 등 특수 상황에 따른 추가 가점입니다.

            3. 카테고리별 5대 분석 지표
            - 신선: 단위 가격, 신선도, 원산지 가치, 가용 부위 비율, 제철 지수
            - 가공: 핵심 원재료 함량, 1회 제공 단가, 첨가물 안전성, 조리 편의성, 보관 효율
            - 기호: 행사(N+1) 효율, 영양 밀도, 브랜드 구현율, 용기 편의성, 대체 가능성
            - 위생: 실질 사용 횟수 단가, 농축도, 화학적 안전성, 내구성/강도, 환경 부담
            - 뷰티: 유효 성분 농도, 피부 자극도, 단위당 가격, 사용 만족도, 브랜드 신뢰도
            - 리빙: 소재의 질, 다목적성, 브랜드 보증(A/S), 에너지 수명, 공간 효율성
            - 펫/라이프: 영양 밸런스, 기호성, 전문 인증, 세트 구성 가치, 트렌드 반영도

            **[입력 데이터]**
            상품명: {productName}
            마트 판매가: {martPrice}
            온라인 최저가: {onlinePrice}
            """;

    private void initChatClient() {
        if (this.chatClient == null) {
            this.chatClient = chatClientBuilder.build();
        }
    }

    public AnalysisAIResponse generateReport(AnalysisRequest request) {
        initChatClient();
        log.info("Generating report for product: {}, martPrice: {}, onlinePrice: {}",
                request.getProductName(), request.getMartPrice(), request.getOnlinePrice());

        // 1. manual variable replacement to avoid PromptTemplate issues with literal
        // braces
        String userMessage = ANALYSIS_PROMPT
                .replace("{productName}", request.getProductName())
                .replace("{martPrice}", String.valueOf(request.getMartPrice()))
                .replace("{onlinePrice}", String.valueOf(request.getOnlinePrice()));

        // 2. Gemini 호출
        String content = null;
        try {
            content = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
            log.info("AI Response received (length: {})", content != null ? content.length() : 0);
            log.debug("AI Response Content: {}", content);

            // Clean up content in case AI wraps it in markdown blocks
            if (content != null) {
                content = content.trim();
                if (content.startsWith("```json")) {
                    content = content.substring(7);
                    if (content.endsWith("```")) {
                        content = content.substring(0, content.length() - 3);
                    }
                } else if (content.startsWith("```")) {
                    content = content.substring(3);
                    if (content.endsWith("```")) {
                        content = content.substring(0, content.length() - 3);
                    }
                }
                content = content.trim();
            }

            AnalysisAIResponse response = objectMapper.readValue(content, AnalysisAIResponse.class);
            log.info("Successfully parsed AI response for product: {}", request.getProductName());

            // 3. If scanLogId is provided, save the report
            if (request.getScanLogId() != null && response != null) {
                scanLogRepository.findById(request.getScanLogId()).ifPresent(scanLog -> {
                    saveAnalysisReport(scanLog, response);
                });
            }

            return response;
        } catch (Exception e) {
            log.error("Failed to generate or parse AI report. Content: {}. Error: {}", content, e.getMessage(), e);
            throw new RuntimeException("AI 분석 리포트 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public AnalysisReport generateReportFromScanLog(com.picpick.entities.ScanLog scanLog) {
        log.info("Generating report from ScanLog ID: {}", scanLog.getId());
        AnalysisRequest request = new AnalysisRequest(
                scanLog.getName(),
                scanLog.getPrice(),
                scanLog.getOnlineItem() != null ? scanLog.getOnlineItem().getItemPrice() : 0,
                scanLog.getId());

        AnalysisAIResponse response = generateReport(request);

        return analysisReportRepository.findByScanLogId(scanLog.getId())
                .orElseGet(() -> saveAnalysisReport(scanLog, response));
    }

    private AnalysisReport saveAnalysisReport(com.picpick.entities.ScanLog scanLog, AnalysisAIResponse response) {
        if (response == null) {
            log.warn("Cannot save AnalysisReport: AI response is null for ScanLog ID: {}", scanLog.getId());
            return null;
        }

        try {
            AnalysisReport report = analysisReportRepository.findByScanLogId(scanLog.getId())
                    .orElse(new AnalysisReport());

            analysisMapper.updateFromResponse(response, report);

            report.setScanLog(scanLog);
            report.setMartPrice(scanLog.getPrice());
            report.setOnlinePrice(scanLog.getOnlineItem() != null ? scanLog.getOnlineItem().getItemPrice() : 0);
            report.setProductName(scanLog.getName());

            AnalysisReport savedReport = analysisReportRepository.save(report);
            log.info("Successfully saved/updated AnalysisReport ID: {} for ScanLog ID: {}", savedReport.getId(),
                    scanLog.getId());
            return savedReport;
        } catch (Exception e) {
            log.error("Failed to save AnalysisReport for ScanLog ID: {}. Error: {}", scanLog.getId(), e.getMessage(),
                    e);
            throw e;
        }
    }
}

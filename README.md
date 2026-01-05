# PicPick BE

## Project Overview
**PicPick**은 오프라인 마트 쇼핑객을 위한 'AI 소비 전략가' 서비스입니다. 사용자가 마트에서 상품을 스캔하면, AI(Gemini)가 온라인 최저가와 비교하여 단순히 가격뿐만 아니라 상품의 가치(성분, 용량, 브랜드 등)를 종합적으로 분석한 **픽리포트(Pick-Report)**를 제공합니다.

## Key Features
- **AI 픽리포트 (VFM 분석)**: 상품명과 가격 데이터를 기반으로 가성비 지수(VFM Index)를 산출하고 5대 지표 분석을 제공합니다.
- **7대 MECE 카테고리 분류**: AI가 상품의 성격에 따라 최적화된 픽단가(Pick Price) 환산 로직을 적용합니다.
- **실시간 위치 기반 마트 매칭**: 사용자의 GPS 좌표를 기반으로 현재 위치한 마트의 행사 정보와 연동됩니다.
- **스캔 이력 관리**: 사용자가 스캔한 모든 상품과 분석 리포트를 히스토리 형태로 관리합니다.
- **마트 정보 자동화**: 마트 관리자가 업로드한 엑셀(XLSX) 파일을 분석하여 상품 데이터베이스를 자동으로 구축합니다.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.8
- **AI Engine**: Spring AI (Google Gemini 1.5 Flash)
- **Database**: MySQL 8.0 / JPA (Hibernate)
- **Migration**: Flyway
- **Mapping**: MapStruct 1.6.2
- **Documentation**: SpringDoc OpenAPI / Swagger UI
---

## System Flow: AI Analysis Sequence

### Logical Flow
1. **요청 수신 (Request Intake)**: 클라이언트가 상품 상세 정보와 `scanLogId`를 포함한 `AnalysisRequest`를 전송.
2. **프롬프트 구성 (Prompt Orchestration)**: `AnalysisService`가 MECE 최적화된 `ANALYSIS_PROMPT`에 실시간 데이터를 치환하여 정교한 분석 프롬프트를 생성.
3. **AI 지능 분석 (AI Intelligence)**: Google Gemini 모델이 입력을 분석하여 고도화된 JSON 형태의 평가 결과를 도출.
4. **데이터 정규화 및 저장 (Data Persistence)**: 
    - AI 응답에서 불필요한 마크다운을 제거하고 `AnalysisAIResponse` 객체로 파싱.
    - `scanLogId`가 있는 경우, 중복 저장을 방지하기 위해 기존 리포트 유무를 확인.
    - `AnalysisMapper`를 통해 최신 분석 결과를 `AnalysisReport` 엔티티에 반영 및 저장.
5. **최종 결과 전달 (Final Delivery)**: 구조화된 분석 데이터를 클라이언트에 반환하여 즉각적인 리포트 화면 구성을 지원.

---
PicPick은 단순히 저렴한 가격이 아닌, **가치(Value)** 중심의 평가를 위해 자체 수식을 사용합니다.

### 가성비 지수 (VFM Index) 수식
$$VFM\Index = \left( \frac{\sum(Mi \times Wi) \times R}{\ln(\text{Price\_Ratio} + e - 1)} \right) \times \prod(\alphaj)$$

- **$M_i, W_i$**: 5대 핵심 지표 점수 및 카테고리별 가중치
- **$R$**: 데이터 신뢰도 (최신성, 리뷰 등)
- **Price_Ratio**: 현재 판매가 / 시장 적정가
- **$\alpha_j$**: 행사(1+1 등), 브랜드 가치 등에 따른 보정 계수

### 7대 MECE 카테고리
1. 신선 식품 (단위 무게당 가격)
2. 가공 식료품 (1회 한 끼당 가격)
3. 기호/음료 (잔/팩당 가격)
4. 생활 위생 (회/롤당 가격)
5. 퍼스널 케어 (단위 사용량당 가격)
6. 홈 리빙 (시간/유지비당 가격)
7. 펫/라이프 (끼니/개당 가격)

## Documentation
- **API Spec**: [api_spec.md](api_spec.md)
- **ER Diagram**: [er_diagram.md](er_diagram.md)
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

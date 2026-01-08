# PicPick Backend (BE)

## 프로젝트 개요
**PicPick**은 마트 쇼핑객을 위한 **'AI 소비 전략가'** 서비스입니다. 
단순한 가격 비교를 넘어, 상품의 가치(성분, 용량, 브랜드 가치 등)를 종합적으로 분석하여 소비자에게 최적의 구매 의사결정을 지원합니다.

Google Gemini AI를 기반으로 한 **픽리포트(Pick-Report)**를 통해 오프라인 마트의 가격이 온라인 최저가 대비 얼마나 합리적인지 즉각적으로 판단해 드립니다.

---

## 핵심 기능

### 1. AI 픽리포트 (Pick-Report)
- **VFM(Value for Money) 분석**: 상품의 실질 가치와 가격을 비교하여 0.0~5.0점 사이의 픽스코어(Pick Score)를 산출합니다.
- **5대 지표 분석**: 카테고리에 맞는 5가지 핵심 지표(품질, 편의성, 가용성 등)를 바탕으로 심층 분석 결과를 제공합니다.

### 2. 고도화된 스캔 프로세싱
- **데이터 재사용 최적화**: 동일 상품에 대해 네이버 최저가 검색 및 AI 분석 결과를 데이터베이스에서 찾아 재사용함으로써 외부 API 호출을 최소화하고 응답 속도를 극대화했습니다.
- **동기/비동기 하이브리드 분석**: 기존 분석 데이터가 있는 경우 즉시 반환(동기)하며, 새로운 상품은 백그라운드에서 분석(비동기)하여 사용자 경험을 최적화합니다.

### 3. 위치 기반 스마트 마트 매칭
- 사용자의 현재 위도/경도를 기반으로 가장 가까운 제휴 마트를 자동으로 매칭합니다.
- 마트 관리자가 업로드한 행사 상품 정보를 실시간으로 반영합니다.

### 4. 7대 MECE 카테고리
- 신선식품, 가공식품, 위생용품 등 상품의 특성에 맞는 7가지 카테고리별 맞춤형 픽단가(Pick Price) 환산 로직을 적용합니다.

---

## Tech Stack

- **Core**: Java 17, Spring Boot 3.5.8
- **AI**: Spring AI (Google Gemini 1.5 Flash)
- **Data**: MySQL 8.0, Spring Data JPA, Hibernate, Flyway
- **External API**: Naver Search API
- **Cloud**: AWS S3 (for Mart Document Files)
- **Utils**: MapStruct, Lombok, Swagger UI (SpringDoc 2.8.5), Dotenv

---

## System Logic

### Scan & Analysis Flow
1. **스캔 요청 (`POST /scan`)**: 사용자가 상품 이름과 가격을 전송합니다.
2. **네이버 검색 & 재사용**: DB에서 최근 1시간 내 동일 상품의 최저가 정보가 있는지 확인합니다. 있으면 재사용, 없으면 네이버 API를 호출합니다.
3. **픽단가 산출**: 기존에 산출된 AI 픽단가가 있으면 이를 즉시 할당합니다.
4. **목록 조회 및 분석 (`GET /scan`)**: 
   - 분석 결과가 없는 상품들에 대해 DB에서 유사 분석 리포트를 검색하여 복제(Cloning)합니다.
   - 여전히 분석이 필요한 상품은 백그라운드 스레드에서 Gemini AI를 통해 분석을 수행합니다.
5. **리포트 확인 (`GET /gemini/{scanId}`)**: 완성된 심층 분석 리포트를 조회합니다.

---

## Documentation

- **API Specification**: [api_spec.md](api_spec.md)
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **User Journey**: [user_journey.md](user_journey.md)

---

## Getting Started

1. `.env` 파일을 생성하고 다음 정보를 입력하세요:
   ```env
   SPRING_AI_GOOGLE_GENAI_API_KEY=your_gemini_api_key
   NAVER_CLIENT_ID=your_naver_id
   NAVER_CLIENT_SECRET=your_naver_secret
   DB_URL=jdbc:mysql://localhost:3306/picpick
   DB_USER=root
   DB_PASSWORD=your_password
   AWS_ACCESS_KEY_ID=your_aws_key
   AWS_SECRET_ACCESS_KEY=your_aws_secret
   ```
2. Gradle 빌드를 실행하세요:
   ```bash
   ./gradlew bootRun
   ```

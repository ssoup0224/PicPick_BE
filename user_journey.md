# PicPick User Journey Flow

이 문서는 사용자가 PicPick 서비스를 이용하며 거치는 단계별 로직과 데이터 흐름을 시각화한 것입니다.

## User Flow

```mermaid
sequenceDiagram
    autonumber
    participant U as User (App)
    participant B as Backend (Spring Boot)
    participant N as Naver Search API
    participant AI as Gemini Pro API
    participant DB as MySQL DB

    Note over U, B: 1. 초기 연동
    U->>B: POST /user/login (UUID)
    B->>DB: 사용자 식별 및 생성
    B-->>U: userId 반환

    Note over U, B: 2. 마트 설정 (위치 기반)
    U->>B: PATCH /user/update-location (Lat, Lng)
    B->>DB: 주변 마트 검색 및 매칭
    B-->>U: 근접 마트 정보 반환

    Note over U, B: 3. 상품 스캔 및 저속 저장
    U->>B: POST /scan (Product List)
    B->>DB: 기존 네이버 데이터 확인
    alt 데이터 재사용 성공
        B->>DB: 기존 가격 정보 할당
    else 데이터 재사용 실패
        B->>N: 네이버 최저가 검색 호출
        B->>DB: 신규 데이터 저장
    end
    B-->>U: 201 Created (초기 정보 반환)

    Note over U, B: 4. 목록 조회 및 AI 분석
    U->>B: GET /scan?userId=XX
    B->>DB: 미분석 상품 기존 리포트 검색
    alt 리포트 복제(Clone) 성공
        B->>DB: 리포트 즉시 연결
    else 새로운 상품
        B-->>U: (먼저 목록 반환)
        B->>AI: analyzeScansBatch 호출 (비동기)
        AI-->>B: 분석 완료 및 저장
    end

    Note over U, AI: 5. 픽리포트 상세 조회
    U->>B: GET /gemini/{scanId}
    B-->>U: 심층 분석 리포트 (VFM Index 포함) 반환
```

---

## 단계별 상세 여정

### 1. 익명 로그인 (UUID 기반)
- 사용자는 번거로운 가입 없이 앱을 켜는 순간 부여된 UUID로 로그인합니다.
- 서버는 UUID를 기반으로 사용자의 마일리지(스캔 횟수) 및 히스토리를 관리합니다.

### 2. 스마트 마트 탐지
- 사용자가 마트에 입장하여 '위치 인증'을 하면 가장 가까운 마트의 DB가 활성화됩니다.
- 해당 마트의 전용 할인 상품(행사 상품) 정보와 연동될 준비를 마칩니다.

### 3. 멀티 스캔 및 데이터 최적화
- 사용자가 여러 상품을 동시에 스캔합니다.
- **최적화 로직**: 
    - 이미 다른 사용자가 스캔했던 상품이라면 네이버 API를 중복 호출하지 않고 예전 데이터를 재사용합니다.
    - 유료 API인 Gemini 호출 이전에, 이미 산출된 '픽단가(단위 가격)'가 있는지도 확인하여 비용을 절감합니다.

### 4. 하이브리드 리포트 생성 (동기+비동기)
- **동기 복제**: 사용자가 스캔 목록을 보는 즉시, 기존에 분석되었던 동일 상품의 리포트를 찾아서 복제(Clone)하여 연결합니다. 사용자는 기다리지 않고 리포트를 바로 볼 수 있습니다.
- **비동기 분석**: 세상에 없던 새로운 상품일 경우에만 백그라운드에서 AI 분석이 시작됩니다.

### 5. 프리미엄 소비 리포트 (Pick-Report)
- 최종적으로 사용자는 VFM(가성비) 점수, 품질 요약, 결론(사라/마라)이 포함된 리포트를 받아봅니다.
- 이 리포트는 추후 동일 상품을 스캔하는 다른 사용자들에게 공유되어 전체 시스템의 속도를 높이는 데 기여합니다.

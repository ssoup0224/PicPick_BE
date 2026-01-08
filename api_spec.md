# 기본 정보

- Base URL: [`http://localhost:8080`](Development)
- **Content-Type:** `application/json`
- Overview: PicPick 애플리케이션을 지원하는 API이며 사용자 관리, 제품 스캔, 가격 분석 및 AI 기반 제품 평가(Gemini) 기능을 제공

---

# Swagger 문서

> API의 대한 문서는 Swagger UI에서 확인
> 
- Swagger 주소: [`http://localhost:8080/swagger-ui/index.html#/`]
    - ip 주소: 

---

# MVP 기능

## User

- RequestMapping = `/user`

### 1. Login (POST)

> 유저의 고유 UUID를 통해 식별한다. 유저가 존재하지 않으면 새로운 유저를 생성한다.
> 
- **Endpoint:** POST /user/login
- **설명:** UUID를 통해 로그인 혹은 회원가입

**Request Body**

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| `uuid` | String | 프론트에서 유저에게 지정한 UUID를 받는다 |

예시:

```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response**

→ 새로운 유저: `201 Created`

예시:

```json
{
  "message": "새로운 유저 생성되었습니다.",
  "userId": 2
}
```

→ 존재한 유저: `200 OK`

예시:

```json
{
  "message": "로그인 성공하였습니다.",
  "userId": 2
}
```

### 2. 위치 변경 (PATCH)

> 현재 위치를 불러오며 제일 가까이 있는 마트와 매칭 해준다.
> 
- **Endpoint**: `PATCH /user/update-location`
- **설명:** 위도경도를 업데이트 하며 현재 있는 마트 인식

**Request Body**

| **Field** | 타입 | 설명 |  |
| --- | --- | --- | --- |
| `userId` | Long | 유저의 ID |  |
| `latitude` | Double | 프론트에서 받아온 위도 값 |  |
| `longitude` | Double | 프론트에서 받아온 경도 값 |  |

예시:

```json
{
  "userId": 1,
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

**Response** `200 OK`

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| `name` | String | 가장 가까운 마트 명 |
| `address` | String | 마트 주소  |

예시:

```json
{
  "name": "픽픽 Mart",
  "address": "경북 포항시 북구 흥해읍 한동로 558"
}
```

## Scan

- RequestMapping = `/scan`

### 1. Save Scans (POST)

> 현재 유저가 스캔한 재품들을 저장하고 백그라운드에서 가격 비교 (네이버) AI 분석에 단위별 가격 (Gemini)을 실행한다.
> 
- **Endpoint:** `POST /scan`
- **설명:** 스캔 된 여러 재품들의 정보 저장

**Request Body**

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| `userId` | Long | 유저 ID |
| `items` | Array | 스캔한 재품 리스트 |
| `items[].scanName` | String | 재품 명 |
| `items[].scanPrice` | Integer | 재품 가격 |

예시: 

```json
{
  "userId": 2,
  "items": [
    {
      "scanName": "비비고깊은사골곰탕 500g",
      "scanPrice": 990
    },
    {
      "scanName": "피죤 4종 각 2500ml",
      "scanPrice": 3480
    }
  ]
}
```

**Response:** `201 Created`

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| `userId` | Long | User ID |
| `scanId` | Long | Scan ID |
| `scanName` | String | 스캔 된 재품 명 |
| `scanPrice` | Integer | 스캔 된 가격 |
| `naverPrice` | Integer | 네이버 최저가 |
| `naverBrand` | String | 재품 브랜드 |
| `naverMaker` | String | 재품 제조사 |
| `naverImage` | String | 재품 이미지 URL |
| `aiUnitPrice` | String | AI로 계산된 단위 가격e (e.g., "1인분에 900원") |
| `isShown` | Boolean | 상품 노출 여부 (기본값: false) |

예시: 

```json
{
    "userId": 2,
    "scanId": 3,
    "scanName": "샤인머스켓 2kg",
    "scanPrice": 7900,
    "naverPrice": 9600,
    "naverBrand": "",
    "naverMaker": "",
    "naverImage": "https://shopping-phinf.pstatic.net/main_8978996/89789963418.7.jpg",
    "aiUnitPrice": "분석 오류",
    "isShown": false
}
```

### 2. Call Scanned Items (GET)

> 유저가 스캔한 재품들을 가져온다. 정보데는 네이버와 Gemini를 통해 받은 데이터가 포함 되어있다. 정보를 가져오는 동시에 가성비 AI 분석 (Gemini)이 실행된다.
> 
- **Endpoint:** `GET /scan`
- **설명:** 유저에게 새캔 된 재품들의 정보를 보낸다.

**Request Parameters**

| **Parameter** | 타입 | 설명 |
| --- | --- | --- |
| `userId` | Long | 유저의 UUID가 아닌 DB에 저장된 ID 값 |

→ 예시: `GET /scan?userId=1`

**Response**: `200 OK`

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| `userId` | Long | User ID |
| `scanId` | Long | Scan ID |
| `scanName` | String | 스캔 된 재품 명 |
| `scanPrice` | Integer | 스캔 된 가격 |
| `naverPrice` | Integer | 네이버 최저가 |
| `naverBrand` | String | 재품 브랜드 |
| `naverMaker` | String | 재품 제조사 |
| `naverImage` | String | 재품 이미지 URL |
| `aiUnitPrice` | String | AI로 계산된 단위 가격e (e.g., "1인분에 900원") |
| `isShown` | Boolean | 상품 노출 여부 (조회 시 true로 자동 변경) |

예시: 

```json
{
    "userId": 2,
    "scanId": 3,
    "scanName": "샤인머스켓 2kg",
    "scanPrice": 7900,
    "naverPrice": 9600,
    "naverBrand": "",
    "naverMaker": "",
    "naverImage": "https://shopping-phinf.pstatic.net/main_8978996/89789963418.7.jpg",
    "aiUnitPrice": "분석 오류",
    "isShown": true
}
```

### 3. Hide Scanned Items (PATCH)

> 유저의 모든 스캔 기록을 숨김 처리한다. (`isShown` 필드를 `false`로 변경)
> 
- **Endpoint:** `PATCH /scan/hide`
- **설명:** 유저의 모든 스캔 아이템 노출 여부 초기화

**Request Parameters**

| **Parameter** | 타입 | 설명 |
| --- | --- | --- |
| `userId` | Long | 유저  ID |

→ 예시: `PATCH /scan/hide?userId=1`

**Response**: `200 OK`

```json
{
  "message": "모든 상품이 숨김 처리되었습니다."
}
```

## Gemini

- RequestMapping = `/gemini`

### 1. Analysis Report (GET)

> AI로 새성된 리포트 정보를 보낸다.
> 
- **Endpoint:** `GET /gemini/{scanId}`
- 설명**:** 특정 스캔 기록의 대한 리포트

**Path Variable**

| **Parameter** | 타입 | **Description** |
| --- | --- | --- |
| `scanId` | Long | 스캔 기록의 ID |

→ 예시: `GET /gemini/1`

**Response:** `200 OK`

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| `naverImage` | String | 재품 이미지 URL |
| `naverBrand` | String | 재품 브랜드 |
| `scanName` | String | 스캔 된 재품 명 |
| `category` | String | 재품 카테고리 |
| `pickScore` | Double | 픽 점수(0.0 - 5.0) |
| `reliabilityScore` | Double | 신뢰도 점수 |
| `scanPrice` | Double | 스캔 된 가격 |
| `naverPrice` | Double | 네이버 최저가 |
| `priceDiff` | Double | 온/오프라인 가격 차이 % |
| `isCheaper` | Boolean | 스캔 된 재품이 더 싼지 여부 |
| `aiUnitPrice` | String | 단위 가격 |
| `indexes` | Array | 5대지표 정보 |
| `qualitySummary` | String | 재풉 질 분석 요약 |
| `priceSummary` | String | 가격 분석 요약 |
| `conclusion` | String | 결론: 상품을 살지 말지 추천 |

예시: 

```json
{
  "naverImage": "https://shopping-phinf.pstatic.net/20200818_1/AA_1597726488344lXqE0_JPEG/51867160759909033_1797305942.jpg?type=w640",
  "naverBrand": "비비고",
  "scanName": "비비고깊은사골곰탕 500g",
  "category": "가공",
  "pickScore": 1.7,
  "reliabilityScore": 0.8,
  "scanPrice": 4400,
  "naverPrice": 950,
  "priceDiff": 363.15,
  "isCheaper": false,
  "aiUnitPrice": "한 끼에 2,200원꼴",
  "indexes": [
    {
      "name": "핵심 원재료 함량",
      "reason": "사골농축액 1.6% (사골추출물 88%) 함유, 호주산 사골 사용으로 품질 유지."
    },
    {
      "name": "1회 제공 단가",
      "reason": "마트가 기준 한 끼 2,200원. 간편식임을 고려 시 보통 수준."
    },
    {
      "name": "첨가물 안전성",
      "reason": "CJ 비비고 브랜드 신뢰도 및 일반적인 성분 구성 고려 시 양호."
    },
    {
      "name": "조리 편의성",
      "reason": "전자레인지 또는 냄비/중탕으로 3~4분 내외 간편 조리 가능."
    },
    {
      "name": "보관 효율",
      "reason": "상온 보관 가능하며 500g 용량으로 1~2인 가구에 적합."
    }
  ],
  "qualitySummary": "비비고 브랜드의 검증된 맛과 품질, 깊고 진한 사골 국물을 간편하게 즐길 수 있는 제품.",
  "priceSummary": "마트 가격 4,400원은 온라인 최저가 950원 대비 363% 비싸 가격 경쟁력이 매우 낮음. 픽단가 또한 온라인 구매 시 훨씬 저렴.",
  "conclusion": "보류 권장 - 품질은 우수하나, 마트 구매 시 가격 메리트가 전혀 없어 온라인 구매를 적극 추천."
}
```

# 부가 기능

## Mart

- RequestMapping = `/mart`

### 1. Signup (POST)

> 새로운 마트 등록.
> 
- **Endpoint:** `POST /mart/register`
- 설명**:** 마트 정보를 입력해서 등록을 한다.

**Request Body**

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| `name` | String | 마트 이름 |
| `address` | String | 마트 주소 |
| `registrationNumber` | BigInteger | 사업자 등록번호 |

에시:

```json
{
  "name": "픽픽 Store",
  "address": "경북 포항시 북구 흥해읍 한동로 558",
  "registrationNumber": 1234567890
}
```

**Response:** `201 Created`

```json
{
"message":"마트 등록 성공하였습니다.",
"martId":6
}
```

### 2. Login (POST)

> 마트 계정 로그인
> 
- **Endpoint:** `Post /mart/login`
- 설명**:** 사업자등록번호로 로그인 (임시설정)

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| `registrationNumber` | BigInteger | 사업자등록번호 |

**Request Body**

```json
{
  "registrationNumber": 12344586911
}
```

**Response**: `200 OK`

```json
{
  "martId": {
    "name": "픽픽 Mart",
    "address": "경북 포항시 북구 흥해읍 한동로 558",
    "registrationNumber": 12344586911,
    "latitude": 0.1,
    "longitude": 0.1
  },
  "message": "로그인 성공하였습니다."
}
```

### 3. Location (PATCH)

> 마트의 위치 정보 업데이트 (등록할 때 `null`로 저장되기에 `PATCH` 사용)
> 
- **Endpoint:** `PATCH /mart/update-location`
- 설명**:** 위도 경도 값 수정

| **Field** | 타입 | 설명 |
| --- | --- | --- |
| userId | Long | User ID |
| latitude | Double | 위도 |
| longitude | Double | 경도 |

**Request Body**

```json
{
  "userId": 1,
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

**Response**: `200 OK`

```json
{
  "message": "위치 정보가 업데이트되었습니다."
}
```

### 4. File Upload (POST)

> 마트 행사 정보 올리기.
> 
- **Endpoint:** `POST /mart/upload-file`
- **Content-Type:** `multipart/form-data`
- **설명:** 엑셀 파일 업로드

**Request Parameters**

| **Parameter** | 타입 | 설명 |
| --- | --- | --- |
| martId | Long | 마크 ID |
| file | MultipartFile | 엑셀 파일 (.xlsx) |

→ 엑셀 파일 형식 예시

| 상품명 | 가격 | 시작일 | 종료일 |
| --- | --- | --- | --- |
| 사과 1kg | 5000 | 2026-01-01 | 2026-01-31 |

**Response:** `201 Created`

```json
{
  "message": "파일 업로드 성공하였습니다."
}
```

### 5. Remove File (DELETE)

> 올린 파일 삭제
> 
- **Endpoint:** `DELETE /mart/{id}/file`
- **설명:** DB에 파일 path와 S3에 파일 삭제

**Path Variable**

| **Parameter** | 타입 | **Description** |
| --- | --- | --- |
| `id` | Long | 마트 ID |

→ 예시: `DELETE /mart/1/file`

**Response:** `200 OK`

```json
{
  "message": "파일이 삭제되었습니다."
}
```

### 6. Mart Info (GET)

> 마트 정보 조회
> 
- **Endpoint:** `GET /mart/{id}`
- **설명:** 상세 정보를 불러온다

**Path Variable**

| **Parameter** | 타입 | **Description** |
| --- | --- | --- |
| `id` | Long | 마트 ID |

→ 예시: `GET /mart/1`

**Response:** `200 OK`

```json
{
  "name": "픽픽 Store",
  "address": "경북 포항시 북구 흥해읍 한동로 558",
  "registrationNumber": 1234567890
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

### 7. Remove Mart (DELETE)

> 마트 삭제.
> 
- **Endpoint:** `DELETE /mart/{id}`
- **설명:** 등록된 마트를 삭제하고 마트에 등록된 재픔들도 같이 삭제된다.

**Path Variable**

| **Parameter** | 타입 | **Description** |
| --- | --- | --- |
| `id` | Long | 마트 ID |

→ 예시: `DELETE /mart/1`

**Response:** `200 OK`

```json
{
  "message": "삭제되었습니다."
}
```

## User

### 1. User Info (GET)

> 유저의 정보 보기
> 
- **Endpoint:** `GET /user/{userId}`
- **설명:** 상세 정보를 불러온다

**Path Variable**

| **Parameter** | 타입 | **Description** |
| --- | --- | --- |
| `userId` | Long | 유저 ID |

→ 예시: `GET /user/1`

**Response:** `200 OK`

```json
{
  "id": 4,
  "uuid": "F412B4C0-1124-4017-85CE-1B5ECA742A9D",
  "createdAt": "2026-01-08T22:06:44.155096",
  "lastLogin": "2026-01-08T22:26:39.147603",
  "totalScans": 0,
  "longitude": 129.39171092468467,
  "latitude": 36.103130720612086
}
```

## Scan

### 1. Remove Scanned Item (GET)

> 스캔 기록 삭제
> 
- **Endpoint:** `DELETE /scan/{id}`
- **설명:** 등록된 스캔 기록 삭제

**Path Variable**

| **Parameter** | 타입 | **Description** |
| --- | --- | --- |
| `id` | Long | scan ID |

→ 예시: `DELETE /scan/1`

**Response:** `200 OK`

```json
{
  "message": "상품 삭제되었습니다."
}
```

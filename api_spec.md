# PicPick API 명세서

## 1. 사용자 (User) API

사용자 생성, 로그인, 위치 인증 및 정보 조회

### 1-1. 게스트 로그인 / 회원가입
- **URL**: `POST /users/login`
- **설명**: 고유 UUID를 기반으로 게스트 로그인을 수행. 처음 방문하는 UUID인 경우 자동으로 회원이 생성됩니다.

**Request Body**
| Key | Type | Description |
| :--- | :--- | :--- |
| uuid | String | 사용자 고유 식별 (필수) |

- **Response**:
    - **신규 회원 (201 Created)**:
    ```json
    {
      "id": 1,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "role": "USER",
      "createdAt": "2024-01-01T12:00:00",
      "lastLogin": "2024-01-15T09:30:00",
      "totalScans": 0,
      "currentLongitude": 127.0,
      "currentLatitude": 37.5
    }
    ```
    - **기존 회원 (200 OK)**: `{"message": "login successful"}`

### 1-2. 사용자 정보 조회
- **URL**: `GET /users/{uuid}`
- **설명**: UUID를 사용하여 특정 사용자의 상세 정보를 조회.

**Path Variables**
| Key | Type | Description |
| :--- | :--- | :--- |
| uuid | String | 조회할 사용자의 고유 식별 (필수) |

- **Response (200 OK)**:
    ```json
    {
      "id": 1,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "role": "USER",
      "createdAt": "2024-01-01T12:00:00",
      "lastLogin": "2024-01-15T09:30:00",
      "totalScans": 42,
      "currentLongitude": 127.123,
      "currentLatitude": 37.567
    }
    ```

### 1-3. 위치 인증 및 현재 마트 할당
- **URL**: `POST /users/location`
- **설명**: 현재 위도/경도를 기반으로 사용자가 마트 내에 있는지 확인하고, 해당 마트를 현재 위치로 할당.

**Request Body**
| Key | Type | Description |
| :--- | :--- | :--- |
| uuid | String | 사용자 고유 식별 (필수) |
| latitude | Double | 사용자의 현재 위도 (필수) |
| longitude | Double | 사용자의 현재 경도 (필수) |

- **Response (200 OK)**:
    ```json
    {
      "id": 101,
      "name": "행복마트",
      "address": "서울시 강남구 테헤란로 123",
      "longitude": 127.12345,
      "latitude": 37.56789,
      "documentFile": "mart_documents/abc.xlsx",
      "createdAt": "2024-01-10T10:00:00",
      "items": [
        {
          "id": 1,
          "name": "사과",
          "price": 2000,
          "startDate": "2024-01-01",
          "endDate": "2024-02-01",
          "discountPercentage": 10
        }
      ]
    }
    ```

### 1-4. 위치 정보 업데이트
- **URL**: `PATCH /users/location/update`
- **설명**: 사용자의 현재 좌표 정보를 업데이트하고 필요한 경우 마트 할당을 갱신.

**Request Body**
| Key | Type | Description |
| :--- | :--- | :--- |
| uuid | String | 사용자 고유 식별 (필수) |
| latitude | Double | 사용자의 현재 위도 (필수) |
| longitude | Double | 사용자의 현재 경도 (필수) |

- **Response (200 OK)**: `MartResponse` (위와 동일)

---

## 2. 마트 (Mart) API

마트 정보 등록 및 관리 기능을 제공.

### 2-1. 마트 등록 (및 상품 리스트 업로드)
- **URL**: `POST /marts/register`
- **설명**: 마트 기본 정보와 상품 목록이 담긴 Excel 파일을 업로드하여 마트를 등록.
- **Content-Type**: `multipart/form-data`

**Request Parameters (Form Data)**
| Key | Type | Description |
| :--- | :--- | :--- |
| name | String | 마트 이름 (필수) |
| address | String | 마트 주소 (필수) |
| brn | String | 사업자 등록 번호 (필수) |
| file | MultipartFile | 상품 목록 엑셀 파일 (.xlsx) (필수) |

- **Response (200 OK)**: `101` (생성된 마트 ID)

---

## 3. 스캔 기록 (Scan Log) API

상품 스캔 데이터를 저장하고 조회.

### 3-1. 스캔 기록 생성
- **URL**: `POST /scanlogs`
- **설명**: 사용자가 스캔한 상품의 이름, 가격, 단위별 가격 기록.

**Request Body**
| Key | Type | Description |
| :--- | :--- | :--- |
| userId | Long | 사용자 시스템 ID (필수) |
| productName | String | 스캔한 상품 이름 (필수) |
| price | Integer | 스캔한 상품 가격 (필수) |
| description | String | 단위별 가격 (옵션) |

- **Response (201 Created)**:
    ```json
    {
      "id": 505,
      "productName": "코카콜라",
      "price": 1500,
      "description": "100ml당 150원",
      "scannedAt": "2024-01-15T14:30:00",
      "martId": 101,
      "martName": "행복마트",
      "onlinePrice": 1200
    }
    ```

### 3-2. 사용자의 스캔 기록 조회
- **URL**: `GET /scanlogs`
- **설명**: 특정 사용자의 전체 스캔 이력을 최신순으로 조회.

**Query Parameters**
| Key | Type | Description |
| :--- | :--- | :--- |
| userId | Long | 조회할 사용자의 시스템 ID (필수) |

- **Response (200 OK)**:
    ```json
    [
      {
        "id": 505,
        "productName": "코카콜라",
        "price": 1500,
        "description": "100ml당 150원",
        "scannedAt": "2024-01-15T14:30:00",
        "martId": 101,
        "martName": "행복마트",
        "onlinePrice": 1200
      },
      {
        "id": 504,
        "productName": "신라면",
        "price": 1200,
        "description": "100g당 120원",
        "scannedAt": "2024-01-14T10:00:00",
        "martId": 101,
        "martName": "행복마트",
        "onlinePrice": 1000
      }
    ]
    ```

---

## 4. AI 분석 및 채팅 API (Gemini)

Google Gemini를 활용한 고도화된 소비 분석 기능을 제공.

### 4-1. 상품 가성비 정밀 분석 (Pick-Report)
- **URL**: `POST /api/chat/analyze`
- **설명**: 상품명, 마트가, 온라인가를 입력하면 AI가 카테고리 분류, 경쟁력 분석, VFM 지수 산출 등을 포함한 리포트를 생성.

**Request Body**
| Key | Type | Description |
| :--- | :--- | :--- |
| productName | String | 분석 대상 상품명 (필수) |
| martPrice | Integer | 현재 마트 판매 가격 (필수) |
| onlinePrice | Integer | 온라인 최저가 또는 비교 가격 (필수) |

- **Response (200 OK)**:
    ```json
    {
      "response": "분석 결과, 이 상품은 마트가가 온라인 최저가보다 약 10% 저렴하여 높은 경쟁력을 가집니다. VFM 지수는 4.5점으로 '강력 추천' 등급입니다. 카테고리는 [신선식품]으로 분류됩니다."
    }
    ```

---

# TASTE RADAR | Java & Spring Boot 기반 REST API 서버

**TASTE RADAR** 서버는 **입맛 기반** 배달 주문 서비스를 위한 Backend 시스템으로, REST API 기반으로 설계되었습니다.  
고객과 사장님 역할에 따라 제공 기능이 구분되며, 가게 검색·주변 가게 조회·장바구니·주문·리뷰·결제까지 배달 주문의 핵심 흐름을 제공합니다.

리뷰에 **5가지 맛(단·짠·신·쓴·감칠)** 평가를 남기면 가게·사용자 입맛 프로필이 쌓이고, **Gemini AI**로 메뉴 추천과 맛 오각형 통계를 제공합니다. **카카오 로그인·카카오페이·카카오톡 주문 알림**을 연동합니다.

| 항목 | 내용 |
|------|------|
| **Project** | TASTE RADAR (입맛 대로 배달) |
| **Service** | 배달 주문 · 맛 프로필 · AI 메뉴 추천 |
| **Team** | 개인 프로젝트 |
| **Period** | — |

---

## 📍 목차

- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [요구사항](#요구사항)
- [설계](#설계)
- [실행 방법](#실행-방법)
- [환경 변수](#환경-변수)
- [프로젝트 구조](#프로젝트-구조)

---

## 주요 기능

### 인증 / 사용자

- 이메일 회원가입 · 로그인 · 로그아웃 (고객 `CUSTOMER` / 사장님 `OWNER`)
- JWT Access · Refresh 토큰 발급 및 갱신 (Refresh Token은 Redis 저장)
- 카카오 OAuth2 소셜 로그인 (`GET /oauth2/authorization/kakao`)
- 프로필 조회 · 닉네임 변경 · 비밀번호 변경 · 배송 주소 수정 · 회원 탈퇴
- 사용자 선호 맛(5축) 설정

### 가게

- 가게 생성 / 수정 / 폐업 / 재오픈 / 조회 (사장님)
- 가게 영업 상태 변경
- 가게명·메뉴명 **Keyword** 검색
- **Haversine** 기반 주변 가게 조회 (`lat`, `lng`, `radiusKm`)
- 리뷰 집계 기반 **가게 맛 프로필** (리뷰 5건 이상 시 노출)
- 주소 → 좌표 변환 (카카오 Geocode API)

### 메뉴

- 메뉴 생성 / 수정 / 삭제 (사장님, 가게별)
- 메뉴 이미지 업로드

### 장바구니

- 메뉴 추가 · 수량 변경 · 항목 삭제 · 전체 비우기 · 조회

### 주문

- 주문 생성 · 주문 내역 조회 · 주문 단건 조회 · 주문 취소 (고객)
- 주문 수락 / 거절 / 상태 변경 (사장님)
- 가게별 주문 목록 · 오늘 주문 통계
- 사장님 대시보드: 주간 매출 · 평점 요약

### 리뷰

- 주문 완료 후 리뷰 작성 (별점 + 메뉴별 5가지 맛 평가)
- 내 리뷰 조회 / 수정 / 삭제
- 가게 리뷰 목록 조회
- 사장님 리뷰 답글 · 리뷰 삭제

### 결제

- **카카오페이** 결제 준비 · 승인 · 취소

### AI · 맛 분석

- 내 리뷰 기반 **맛 오각형** 통계 (`/api/ai/me/taste-pentagon`, Redis 캐시)
- 선호 입맛 기반 **Gemini 메뉴 추천** (`/api/ai/stores/{storeId}/recommendations`)

### 알림

- 인앱 알림 목록 · 읽음 처리 · 미읽음 개수
- 주문 이벤트 시 **카카오톡** 메시지 발송 (선택)

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4 · Spring Security · Spring Data JPA |
| Auth | JWT · OAuth2 Client (Kakao) |
| DB | MySQL |
| Cache | Redis (Refresh Token · AI 응답 캐시) |
| Query | QueryDSL 7 |
| AI | Google Gemini API |
| External | Kakao (Login · Pay · REST Geocode · Talk) |
| Build | Gradle |

---

## 요구사항

| 항목 | 버전 / 비고 |
|------|-------------|
| JDK | 21+ |
| MySQL | 8.x (`taste_radar` 스키마) |
| Redis | 6379 (로컬 기본) |
| Gradle | Wrapper 포함 (`./gradlew`) |

선택 연동 (기능별):

| 연동 | 용도 |
|------|------|
| 카카오 Developers | OAuth 로그인 · Geocode · Talk |
| 카카오페이 | 결제 |
| Google AI (Gemini) | 메뉴 추천 |

---

## 설계

| 문서 | 링크 |
|------|------|
| 요구사항 명세서 | _(추가 예정)_ |
| 와이어프레임 | _(추가 예정)_ |
| API 명세서 | _(추가 예정)_ |
| ERD | _(추가 예정)_ |

---

## 실행 방법

```bash
# demo 디렉터리에서
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

기본 포트: `http://localhost:8080`

테스트:

```bash
./gradlew test
```

---

## 환경 변수

`demo/.env` 또는 프로젝트 루트 `.env` 파일로 주입합니다 (`application.yml`에서 optional import).

| 변수 | 설명 |
|------|------|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | DB 사용자 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET` | HS256용 시크릿 (256bit 이상 권장) |
| `KAKAO_CLIENT_ID` | 카카오 OAuth Client ID |
| `KAKAO_CLIENT_SECRET` | 카카오 OAuth Client Secret |
| `KAKAO_REST_API_KEY` | 주소 Geocode 등 REST API |
| `KAKAO_PAY_ADMIN_KEY` | 카카오페이 Admin Key |
| `KAKAO_PAY_CID` | 가맹점 CID (기본: 테스트 `TC0ONETIME`) |
| `GEMINI_API_KEY` | Gemini API Key |
| `DEMO_SEED_ENABLED` | `true` 시 기동 시 강남 `[데모]` 가게·메뉴 시드 (기본 `false`) |
| `DEMO_SEED_RESET` | `true` 시 기존 `[데모]` 데이터 삭제 후 재시드 (기본 `false`) |
| `DEMO_DEFAULT_RADIUS_KM` | 데모 API 기본 반경 km (기본 `3`) |
| `UPLOAD_DIR` | 업로드 저장 경로 (기본: `./uploads`) |
| `UPLOAD_PUBLIC_BASE_URL` | 업로드 파일 공개 URL prefix |

---

## API 개요

| 구분 | Base Path | 설명 |
|------|-----------|------|
| 인증 | `/api/auth` | register, login, refresh, logout |
| 카카오 | `/api/auth/kakao`, `/oauth2/authorization/kakao` | 소셜 로그인 |
| 사용자 | `/api/users` | 프로필 · 닉네임 · 주소 · 맛 선호 |
| 가게 (고객) | `/api/stores` | 검색 · 주변 · 상세 · 리뷰 |
| 가게 (사장) | `/api/owner/stores` | CRUD · 영업 상태 |
| 메뉴 | `/api/owner/stores/{storeId}/menus` | CUD |
| 장바구니 | `/api/cart` | CRUD |
| 주문 (고객) | `/api/orders` | 생성 · 조회 · 취소 |
| 주문 (사장) | `/api/owner/orders` | 수락 · 거절 · 상태 · 통계 |
| 대시보드 | `/api/owner/dashboard` | 주간 매출 · 평점 |
| 리뷰 | `/api/reviews`, `/api/orders/{orderId}/reviews` | CRUD |
| 결제 | `/api/payments/kakaopay` | ready · approve · cancel |
| AI | `/api/ai` | 맛 오각형 · 메뉴 추천 |
| 알림 | `/api/notifications` | 목록 · 읽음 |
| 업로드 | `/api/owner/uploads` | 이미지 |

---

## 프로젝트 구조

```
src
├── main
│   ├── java/com/tasteradar
│   │   ├── TasteRadarApplication.java
│   │   │
│   │   ├── domain
│   │   │   ├── ai                    # Gemini 맛 오각형 · 메뉴 추천
│   │   │   │   ├── api
│   │   │   │   └── service
│   │   │   ├── cart                  # 장바구니
│   │   │   │   ├── api
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── menu                  # 메뉴 (사장님)
│   │   │   │   ├── api
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── notification          # 인앱 알림 · 카카오톡
│   │   │   │   ├── api
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── order                 # 주문 · 대시보드
│   │   │   │   ├── api
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── payment               # 카카오페이
│   │   │   │   ├── api
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── review                # 맛 리뷰
│   │   │   │   ├── api
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── store                 # 가게 · Geocode · 맛 집계
│   │   │   │   ├── api
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── upload                # 이미지 업로드
│   │   │   │   ├── api
│   │   │   │   └── service
│   │   │   └── user                  # 사용자 프로필
│   │   │       ├── api
│   │   │       ├── entity
│   │   │       ├── repository
│   │   │       └── service
│   │   │
│   │   ├── global                    # 공통 설정 · 예외 처리
│   │   │   ├── api
│   │   │   ├── config
│   │   │   └── entity
│   │   │
│   │   ├── oauth/kakao               # 카카오 OAuth 핸들러
│   │   └── security                  # JWT · 인증 · Refresh Token
│   │       ├── api
│   │       ├── config
│   │       ├── filter
│   │       ├── provider
│   │       └── service
│   │
│   └── resources
│       └── application.yml
│
└── test
    └── java/com/tasteradar
        └── TasteRadarApplicationTests.java
```

---

## 주문 상태 흐름

```
PENDING(주문 확인) → COOKING(조리 중) → DELIVERING(배달 중) → DELIVERED(배달 완료)
                  ↘ REJECTED(거절) / CANCELED(취소)
```

---

## Demo (포트폴리오 · 면접관용)

면접관 위치를 미리 알 수 없으므로, **강남역 일대 고정 좌표**에 샘플 가게를 두고 프론트에서 **「데모: 강남역 주변」** 위치를 쓰도록 합니다.

| 항목 | 값 |
|------|-----|
| 기준 좌표 | 위도 `37.497942`, 경도 `127.027621` (강남역) |
| 기본 반경 | `3` km (`radiusKm`) |
| 주변 가게 API | `GET /api/stores/nearby?lat=37.497942&lng=127.027621&radiusKm=3` |
| 데모 메타 API | `GET /api/demo` · `GET /api/demo/locations` |

### 운영 DB에 샘플 데이터 넣기

EC2 `app.env`에 한 줄 추가 후 서비스 재시작:

```bash
DEMO_SEED_ENABLED=true
# 이미 예전 시드를 넣었다면, 이미지·리뷰 포함 버전으로 다시 넣을 때:
DEMO_SEED_RESET=true
```

- 가게 **5곳**, 메뉴 **15개**, 가게당 **리뷰 6개**(맛 프로필 노출용), **가게·메뉴 사진 URL**(picsum)
- `[데모]` 데이터가 **없을 때만** 삽입. 이미 있으면 스킵 (`DEMO_SEED_RESET=true`면 삭제 후 재삽입)
- 넣은 뒤에는 `DEMO_SEED_ENABLED=false` 로 바꿔도 됩니다

### 프론트엔드 연동 (taste-radar-FE)

`GET /api/demo/locations` 응답 예:

```json
[
  {
    "id": "gangnam-station",
    "label": "데모: 강남역 주변",
    "lat": 37.497942,
    "lng": 127.027621,
    "radiusKm": 3,
    "recommended": true
  }
]
```

버튼 클릭 시 세션에 저장 후 주변 가게를 다시 불러옵니다:

```javascript
const DEMO_SESSION_KEY = "tasteRadar.nearbySession";

export async function applyDemoLocation(locationId = "gangnam-station") {
  const res = await fetch("/api/demo/locations");
  if (!res.ok) throw new Error("데모 위치를 불러오지 못했습니다.");
  const locations = await res.json();
  const loc =
    locations.find((l) => l.id === locationId) ||
    locations.find((l) => l.recommended) ||
    locations[0];
  sessionStorage.setItem(
    DEMO_SESSION_KEY,
    JSON.stringify({
      lat: loc.lat,
      lng: loc.lng,
      radiusKm: loc.radiusKm,
      savedAt: Date.now(),
    })
  );
  return loc;
}
```

메인/온보딩 화면에 **「데모: 강남역 주변」** 버튼을 두고 `applyDemoLocation()` 호출 후 기존 nearby 목록 로직을 실행하면 됩니다.

---

## 라이선스

개인 학습·포트폴리오 프로젝트입니다.

# TASTE RADAR | Java & Spring Boot 기반 REST API 서버

**TASTE RADAR** 서버는 **입맛 기반** 배달 주문 서비스를 위한 Backend 시스템으로, REST API 기반으로 설계되었습니다.  
고객과 사장님 역할에 따라 제공 기능이 구분되며, 가게 검색·주변 가게 조회·장바구니·주문·리뷰·결제까지 배달 주문의 핵심 흐름을 제공합니다.

리뷰에 **5가지 맛(단·짠·신·쓴·감칠)** 평가를 남기면 가게·사용자 입맛 프로필이 쌓이고, **Gemini AI**로 메뉴 추천과 맛 오각형 통계를 제공합니다. **카카오 로그인·카카오페이·카카오톡 주문 알림**을 연동합니다.

### 🔗 관련 산출물
[![Frontend Repository](https://img.shields.io/badge/Frontend_Repo-61DAFB?style=flat-square&logo=react&logoColor=black)](https://github.com/gaebarja99/taste-radar-FE.git) [![Presentation PPT](https://img.shields.io/badge/Presentation_PPT-D14836?style=flat-square)](https://canva.link/q7ztzu8r36yatno)

![Project](https://img.shields.io/badge/Project-TasteRadar%20(Backend)-orange)
![Service](https://img.shields.io/badge/Service-배달%20주문%20REST%20API-blue)
![Period](https://img.shields.io/badge/Period-2026.05.08~2026.05.17-green)

---

## 📍 목차
[![주요 기능](https://img.shields.io/badge/주요%20기능-FF6B6B?style=for-the-badge)](#주요-기능)
[![기술 스택](https://img.shields.io/badge/기술%20스택-4DABF7?style=for-the-badge)](#기술-스택)
[![설계](https://img.shields.io/badge/설계-845EF7?style=for-the-badge)](#설계)
[![프로젝트 구조](https://img.shields.io/badge/프로젝트%20구조-ADB5BD?style=for-the-badge)](#프로젝트-구조)

---

## 주요 기능
### 인증 · 회원
- 이메일 회원가입 / 로그인 / 로그아웃
- 카카오 OAuth2 소셜 로그인 (고객 · 사장)
- JWT 토큰 발급 / 갱신
- 프로필 조회, 닉네임 · 주소 · 비밀번호 수정
- 회원 탈퇴
### 입맛
- 입맛 5축 선호 설정 (단 · 짠 · 신 · 쓴 · 감칠)
- 내 리뷰 기반 맛 오각형 통계
### 가게 관리 (사장)
- 가게 생성 / 수정 / 삭제 / 조회
- 가게 영업 상태 변경 (준비 · 영업 · 마감)
- 폐업 가게 재오픈
- 내 가게 목록 조회
- 주소 지오코딩 (좌표 저장)
- 가게 이미지 업로드
### 가게 · 메뉴 (고객)
- Keyword 기반 가게 검색 (가게명 · 메뉴명)
- GPS 기반 주변 가게 조회
- 가게 상세 · 메뉴 목록 조회
- 가게 맛 프로필 조회 (리뷰 집계)
- 가게 리뷰 목록 조회
### 메뉴 관리 (사장)
- 메뉴 생성 / 수정 / 삭제
- 메뉴 이미지 등록
### 장바구니
- 메뉴 추가
- 항목 수량 변경
- 항목 삭제
- 장바구니 조회 / 비우기
### 주문 (고객)
- 주문 생성
- 주문 내역 · 상세 조회
- 주문 취소 (접수 대기만)
### 주문 (사장)
- 주문 목록 · 상세 조회
- 주문 수락 / 거절 (거절은 접수 대기만)
- 주문 상태 변경 (조리 · 배달 · 완료)
- 오늘 주문 통계
### 결제
- 카카오페이 결제 준비 / 승인 / 취소
### 대시보드 (사장)
- 주간 매출 조회
- 평점 · 리뷰 요약
### 리뷰
- 주문 후 리뷰 작성 (별점 + 맛 태그)
- 내 리뷰 조회 / 수정 / 삭제
- 사장 답글 작성 / 리뷰 삭제
### AI
- 가게별 입맛 기반 메뉴 추천 (Gemini)
### 알림
- 주문 상태 인앱 알림 조회 / 읽음 처리

---

## 기술 스택

<img src="https://img.shields.io/badge/Java_21-007396?style=for-the-badge&logo=Java&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=Spring-Boot&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=Spring&logoColor=white"> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL-007ACC?style=for-the-badge&logoColor=white">
---

## 🛠️ Architecture & Design

<details>
<summary><b>📊 데이터베이스 설계 (ERD) 보기</b></summary>
<br>

<img width="100%" alt="TasteRadar ERD" src="https://github.com/user-attachments/assets/b8198b6c-8f0f-4ee2-8c5e-f0d6049925dd" />

</details>

<details>
<summary><b>📋 요구사항 명세서 보기</b></summary>
<br>

* 📝 **TasteRadar 요구사항 명세서 (Notion):** [바로가기](https://full-airplane-477.notion.site/35c65ed6701d8014a75ef11745136984?source=copy_link)

</details>

<details>
<summary><b>🚀 API 명세서 보기</b></summary>
<br>

* 📡 **TasteRadar API 명세서 (Notion):** [바로가기](https://full-airplane-477.notion.site/API-35d65ed6701d808db5c4fe0c7754263d?source=copy_link)

</details>


---

### 📂 프로젝트 구조

```text
src/main/java/com/tasteradar
├── ai            # Gemini LLM 기반 입맛 추천 및 캐싱 로직
├── cart          # 장바구니 관리 기능
├── menu          # 가게 메뉴 관리 (Owner 전용)
├── notification  # 주문 현황 카카오톡 알림톡 발송 서비스
├── order         # 주문 생성, 처리 및 사장님 대시보드 매출 통계
├── payment       # 카카오페이(KakaoPay) 결제 연동 및 승인/취소
├── review        # 리뷰 작성 및 리뷰 기반 맛 취향 데이터 집계 (QueryDSL)
├── store         # 카카오 로컬 지오코딩 연동 매장 검색 및 정보 관리
├── security      # JWT + Redis 연동 무상태 인증 시스템
└── oauth         # 카카오 소셜 로그인 연동


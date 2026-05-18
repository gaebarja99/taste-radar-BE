# TASTE RADAR | Java & Spring Boot 기반 REST API 서버

**TASTE RADAR** 서버는 **입맛 기반** 배달 주문 서비스를 위한 Backend 시스템으로, REST API 기반으로 설계되었습니다.  
고객과 사장님 역할에 따라 제공 기능이 구분되며, 가게 검색·주변 가게 조회·장바구니·주문·리뷰·결제까지 배달 주문의 핵심 흐름을 제공합니다.

리뷰에 **5가지 맛(단·짠·신·쓴·감칠)** 평가를 남기면 가게·사용자 입맛 프로필이 쌓이고, **Gemini AI**로 메뉴 추천과 맛 오각형 통계를 제공합니다. **카카오 로그인·카카오페이·카카오톡 주문 알림**을 연동합니다.

![Project](https://img.shields.io/badge/Project-EAT%20NOW(Backend)-orange)
![Service](https://img.shields.io/badge/Service-배달%20주문%20REST%20API-blue)
![Team](https://img.shields.io/badge/Team-2인%20프로젝트-purple)
![Period](https://img.shields.io/badge/Period-2026.04.06~2026.05.08-green)

---

## 📍 목차
[![주요 기능](https://img.shields.io/badge/주요%20기능-FF6B6B?style=for-the-badge)](#주요-기능)
[![기술 스택](https://img.shields.io/badge/기술%20스택-4DABF7?style=for-the-badge)](#기술-스택)
[![설계](https://img.shields.io/badge/설계-845EF7?style=for-the-badge)](#설계)
[![프로젝트 구조](https://img.shields.io/badge/프로젝트%20구조-ADB5BD?style=for-the-badge)](#프로젝트-구조)

---

## 주요 기능


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





## 프로젝트 구조

src
+---main
|   +---java
|   |   \---com
|   |       \---tasteradar
|   |           |   TasteRadarApplication.java
|   |           |
|   |           +---domain
|   |           |   +---ai
|   |           |   |   +---api
|   |           |   |   |   |   AiController.java
|   |           |   |   |   \---dto
|   |           |   |   |           StoreRecommendationsResponse.java
|   |           |   |   |           TastePentagonResponse.java
|   |           |   |   \---service
|   |           |   |       |   AiCacheService.java
|   |           |   |       |   AiService.java
|   |           |   |       |   GeminiRecommendationClient.java
|   |           |   |       \---dto
|   |           |   |               LlmMenuRecommendation.java
|   |           |   |               MenuRecommendContext.java
|   |           |   |
|   |           |   +---cart
|   |           |   |   +---api
|   |           |   |   |   |   CartController.java
|   |           |   |   |   \---dto
|   |           |   |   |           CartAddRequest.java
|   |           |   |   |           CartItemResponse.java
|   |           |   |   |           CartQuantityPatchRequest.java
|   |           |   |   |           CartResponse.java
|   |           |   |   +---entity
|   |           |   |   |       Cart.java
|   |           |   |   |       CartItem.java
|   |           |   |   +---repository
|   |           |   |   |       CartRepository.java
|   |           |   |   \---service
|   |           |   |           CartService.java
|   |           |   |
|   |           |   +---menu
|   |           |   |   +---api
|   |           |   |   |   |   OwnerMenuController.java
|   |           |   |   |   \---dto
|   |           |   |   |           MenuCreateRequest.java
|   |           |   |   |           MenuResponse.java
|   |           |   |   |           MenuUpdateRequest.java
|   |           |   |   +---entity
|   |           |   |   |       Menu.java
|   |           |   |   +---repository
|   |           |   |   |       MenuRepository.java
|   |           |   |   \---service
|   |           |   |           MenuCommandService.java
|   |           |   |
|   |           |   +---notification
|   |           |   |   +---api
|   |           |   |   |   |   NotificationController.java
|   |           |   |   |   \---dto
|   |           |   |   |           NotificationResponse.java
|   |           |   |   +---entity
|   |           |   |   |       Notification.java
|   |           |   |   +---repository
|   |           |   |   |       NotificationRepository.java
|   |           |   |   \---service
|   |           |   |           KakaoTalkOrderMessageService.java
|   |           |   |           NotificationService.java
|   |           |   |           OrderNotificationService.java
|   |           |   |
|   |           |   +---order
|   |           |   |   +---api
|   |           |   |   |   |   OrderController.java
|   |           |   |   |   |   OwnerDashboardController.java
|   |           |   |   |   |   OwnerOrderController.java
|   |           |   |   |   \---dto
|   |           |   |   |           DailySalesPointDto.java
|   |           |   |   |           OrderActionResponse.java
|   |           |   |   |           OrderCancelRequest.java
|   |           |   |   |           OrderCreateRequest.java
|   |           |   |   |           OrderDetailResponse.java
|   |           |   |   |           OrderItemResponse.java
|   |           |   |   |           OrderSummaryResponse.java
|   |           |   |   |           OwnerOrderStatusPatchRequest.java
|   |           |   |   |           OwnerRatingSummaryResponse.java
|   |           |   |   |           OwnerRejectRequest.java
|   |           |   |   |           StoreOrderStatDto.java
|   |           |   |   |           TodayOrderCountResponse.java
|   |           |   |   |           WeeklySalesResponse.java
|   |           |   |   +---entity
|   |           |   |   |       FoodOrder.java
|   |           |   |   |       OrderItem.java
|   |           |   |   |       OrderStatus.java
|   |           |   |   +---repository
|   |           |   |   |       FoodOrderRepository.java
|   |           |   |   \---service
|   |           |   |           OrderItemMenuSupport.java
|   |           |   |           OrderService.java
|   |           |   |           OrderSummaryMapper.java
|   |           |   |           OwnerDashboardService.java
|   |           |   |           OwnerOrderService.java
|   |           |   |
|   |           |   +---payment
|   |           |   |   +---api
|   |           |   |   |   |   PaymentController.java
|   |           |   |   |   \---dto
|   |           |   |   |           KakaoPayApproveRequest.java
|   |           |   |   |           KakaoPayApproveResponse.java
|   |           |   |   |           KakaoPayCancelRequest.java
|   |           |   |   |           KakaoPayReadyRequest.java
|   |           |   |   |           KakaoPayReadyResponse.java
|   |           |   |   +---entity
|   |           |   |   |       Payment.java
|   |           |   |   +---repository
|   |           |   |   |       PaymentRepository.java
|   |           |   |   \---service
|   |           |   |           KakaoPayService.java
|   |           |   |
|   |           |   +---review
|   |           |   |   +---api
|   |           |   |   |   |   OrderReviewController.java
|   |           |   |   |   |   OwnerReviewController.java
|   |           |   |   |   |   ReviewController.java
|   |           |   |   |   \---dto
|   |           |   |   |           MyReviewResponse.java
|   |           |   |   |           OwnerReplyRequest.java
|   |           |   |   |           ReviewCreateRequest.java
|   |           |   |   |           ReviewMenuTasteItemDto.java
|   |           |   |   |           ReviewMenuTasteResponse.java
|   |           |   |   |           ReviewTasteDto.java
|   |           |   |   |           ReviewUpdateRequest.java
|   |           |   |   |           StoreReviewResponse.java
|   |           |   |   +---entity
|   |           |   |   |       Review.java
|   |           |   |   |       ReviewMenuTasteEntry.java
|   |           |   |   |       TasteType.java
|   |           |   |   +---repository
|   |           |   |   |       ReviewRepository.java
|   |           |   |   |       StoreTasteAggregateProjection.java
|   |           |   |   |       StoreTasteBatchProjection.java
|   |           |   |   \---service
|   |           |   |           ReviewService.java
|   |           |   |
|   |           |   +---store
|   |           |   |   +---api
|   |           |   |   |   |   OwnerGeocodeController.java
|   |           |   |   |   |   OwnerStoreController.java
|   |           |   |   |   |   StoreController.java
|   |           |   |   |   \---dto
|   |           |   |   |           GeocodingResponse.java
|   |           |   |   |           OwnerStoreCreateRequest.java
|   |           |   |   |           OwnerStoreSummaryResponse.java
|   |           |   |   |           OwnerStoreUpdateRequest.java
|   |           |   |   |           StoreCreatedResponse.java
|   |           |   |   |           StoreDetailResponse.java
|   |           |   |   |           StoreImageUrlResponse.java
|   |           |   |   |           StoreMenuResponse.java
|   |           |   |   |           StoreStatusPatchRequest.java
|   |           |   |   |           StoreStatusResponse.java
|   |           |   |   |           StoreSummaryResponse.java
|   |           |   |   |           StoreTasteHighlightResponse.java
|   |           |   |   |           StoreTasteProfileResponse.java
|   |           |   |   +---entity
|   |           |   |   |       Store.java
|   |           |   |   |       StoreImage.java
|   |           |   |   |       StoreStatus.java
|   |           |   |   +---repository
|   |           |   |   |       StoreRepository.java
|   |           |   |   |       StoreSearchRepository.java
|   |           |   |   |       StoreSearchRepositoryImpl.java
|   |           |   |   \---service
|   |           |   |           KakaoGeocodingService.java
|   |           |   |           OwnerStoreService.java
|   |           |   |           StoreService.java
|   |           |   |           StoreTasteService.java
|   |           |   |
|   |           |   +---upload
|   |           |   |   +---api
|   |           |   |   |       OwnerUploadController.java
|   |           |   |   \---service
|   |           |   |           LocalFileStorageService.java
|   |           |   |
|   |           |   \---user
|   |           |       +---api
|   |           |       |   |   UserController.java
|   |           |       |   \---dto
|   |           |       |           AddressUpdateRequest.java
|   |           |       |           NicknameUpdateRequest.java
|   |           |       |           PasswordUpdateRequest.java
|   |           |       |           TastePreferencesResponse.java
|   |           |       |           TasteUpdateRequest.java
|   |           |       |           UserProfileResponse.java
|   |           |       +---entity
|   |           |       |       User.java
|   |           |       |       UserRole.java
|   |           |       |       UserTastePreference.java
|   |           |       +---repository
|   |           |       |       UserRepository.java
|   |           |       \---service
|   |           |               UserOAuthService.java
|   |           |               UserProfileService.java
|   |           |
|   |           +---global
|   |           |   +---api
|   |           |   |       GlobalExceptionHandler.java
|   |           |   +---config
|   |           |   |       JpaAuditingConfig.java
|   |           |   |       QuerydslConfig.java
|   |           |   |       UploadProperties.java
|   |           |   |       WebMvcConfig.java
|   |           |   \---entity
|   |           |           BaseCreatedAtEntity.java
|   |           |           BaseTimeEntity.java
|   |           |
|   |           +---oauth
|   |           |   \---kakao
|   |           |       |   KakaoLoginStartController.java
|   |           |       |   KakaoOAuth2LoginSuccessHandler.java
|   |           |       |   KakaoOAuth2Service.java
|   |           |       |   KakaoOAuth2ServiceImpl.java
|   |           |       |   KakaoUserAttributeMapper.java
|   |           |       |   KakaoUserProfile.java
|   |           |       \---dto
|   |           |               KakaoLoginTokenResponse.java
|   |           |
|   |           \---security
|   |               +---api
|   |               |   |   AuthController.java
|   |               |   \---dto
|   |               |           AuthTokenResponse.java
|   |               |           LoginRequest.java
|   |               |           RegisterRequest.java
|   |               |           TokenRefreshRequest.java
|   |               |           TokenRefreshResponse.java
|   |               +---config
|   |               |       JwtProperties.java
|   |               |       SecurityConfig.java
|   |               +---filter
|   |               |       JwtAuthenticationFilter.java
|   |               +---provider
|   |               |       JwtTokenProvider.java
|   |               \---service
|   |                       LocalAuthService.java
|   |                       RefreshTokenService.java
|   |
|   +---generated                 (QueryDSL Q-class, build only)
|   |
|   \---resources
|           application.yml
|
\---test
    \---java
        \---com
            \---tasteradar
                    TasteRadarApplicationTests.java




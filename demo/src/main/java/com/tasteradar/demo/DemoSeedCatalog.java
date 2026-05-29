package com.tasteradar.demo;

import java.time.LocalTime;
import java.util.List;

/**
 * 포트폴리오 데모용 강남역 주변 샘플 데이터 정의.
 * 이미지는 picsum.photos 시드 URL(외부 HTTPS)을 사용합니다.
 */
public final class DemoSeedCatalog {

	public static final String STORE_NAME_PREFIX = "[데모] ";
	public static final String DEMO_OWNER_EMAIL = "demo-owner@taste-radar.portfolio";
	public static final String DEMO_CUSTOMER_EMAIL_PREFIX = "demo-customer-";

	/** 강남역 (2호선) 대표 좌표 */
	public static final double GANGNAM_STATION_LAT = 37.497942;
	public static final double GANGNAM_STATION_LNG = 127.027621;

	private DemoSeedCatalog() {
	}

	public static List<DemoLocationPreset> locations(double defaultRadiusKm) {
		return List.of(
				new DemoLocationPreset(
						"gangnam-station",
						"데모: 강남역 주변",
						GANGNAM_STATION_LAT,
						GANGNAM_STATION_LNG,
						defaultRadiusKm,
						true
				)
		);
	}

	public static List<DemoStoreSeed> stores() {
		return List.of(
				store(
						"korean",
						"강남 한식당",
						"서울 강남구 강남대로 지하 396",
						"강남역 2번 출구",
						37.4985,
						127.0280,
						List.of(storeImg("korean", 1), storeImg("korean", 2)),
						List.of(
								menu("korean", "main", "한식 정식", 12000, "제철 반찬 6가지"),
								menu("korean", "soup", "된장찌개 단품", 9000, "국내산 두부"),
								menu("korean", "rice", "불고기 덮밥", 11000, "달짝한 양념")
						),
						koreanReviews()
				),
				store(
						"pasta",
						"강남 파스타",
						"서울 강남구 역삼동 테헤란로 152",
						"강남역 인근",
						37.4968,
						127.0255,
						List.of(storeImg("pasta", 1), storeImg("pasta", 2)),
						List.of(
								menu("pasta", "olio", "알리오 올리오", 13000, "올리브 오일 파스타"),
								menu("pasta", "gnocchi", "크림 뇨끼", 14000, "수제 뇨끼"),
								menu("pasta", "pizza", "마르게리타 피자", 16000, "모짜렐라 치즈")
						),
						pastaReviews()
				),
				store(
						"kimbap",
						"강남 김밥천국",
						"서울 강남구 강남대로 390",
						"지하상가 B1",
						37.4992,
						127.0301,
						List.of(storeImg("kimbap", 1)),
						List.of(
								menu("kimbap", "roll", "참치김밥", 4500, "인기 메뉴"),
								menu("kimbap", "rabokki", "라볶이", 5500, "매콤한 소스"),
								menu("kimbap", "noodle", "잔치국수", 6000, "얼큰한 육수")
						),
						kimbapReviews()
				),
				store(
						"burger",
						"강남 버거",
						"서울 강남구 역삼동 강남대로 지하 396",
						"푸드코트 12호",
						37.4955,
						127.0268,
						List.of(storeImg("burger", 1), storeImg("burger", 2)),
						List.of(
								menu("burger", "classic", "클래식 버거", 9800, "숙성 패티"),
								menu("burger", "fries", "감자튀김", 3500, "바삭하게"),
								menu("burger", "double", "더블 치즈 버거", 11500, "체다 2장")
						),
						burgerReviews()
				),
				store(
						"dessert",
						"강남 디저트",
						"서울 강남구 강남대로 382",
						"1층",
						37.5001,
						127.0240,
						List.of(storeImg("dessert", 1), storeImg("dessert", 2)),
						List.of(
								menu("dessert", "croffle", "크로플", 7500, "아이스크림 토핑"),
								menu("dessert", "latte", "말차 라떼", 5500, "국내산 말차"),
								menu("dessert", "cake", "치즈케이크", 6800, "뉴욕 스타일")
						),
						dessertReviews()
				)
		);
	}

	private static String storeImg(String key, int index) {
		return "https://picsum.photos/seed/taste-radar-store-" + key + "-" + index + "/800/500";
	}

	private static String menuImg(String storeKey, String menuKey) {
		return "https://picsum.photos/seed/taste-radar-menu-" + storeKey + "-" + menuKey + "/400/300";
	}

	private static DemoStoreSeed store(
			String key,
			String nameSuffix,
			String address,
			String addressDetail,
			double lat,
			double lng,
			List<String> storeImages,
			List<DemoMenuSeed> menus,
			List<DemoReviewSeed> reviews
	) {
		return new DemoStoreSeed(
				STORE_NAME_PREFIX + nameSuffix,
				address,
				addressDetail,
				lat,
				lng,
				10000,
				LocalTime.of(9, 0),
				LocalTime.of(22, 0),
				25,
				storeImages,
				menus,
				reviews
		);
	}

	private static DemoMenuSeed menu(String storeKey, String menuKey, String name, long price, String description) {
		return new DemoMenuSeed(name, price, description, menuImg(storeKey, menuKey));
	}

	private static List<DemoReviewSeed> koreanReviews() {
		return List.of(
				review(5, "반찬이 정말 깔끔해요. 집밥 느낌이 나서 좋았어요.", "sweet", null),
				review(4, "된장찌개가 구수합니다. 밥 한 공기 뚝딱이에요.", "umami", "감사합니다! 다음에도 맛있게 드세요."),
				review(5, "불고기 덮밥 양념이 제 스타일이에요.", "sweet", null),
				review(4, "가격 대비 푸짐해요. 점심 특선 추천합니다.", "salty", null),
				review(5, "직원분이 친절하고 음식이 빨리 나왔어요.", "umami", null),
				review(4, "매장이 깨끗하고 메뉴 사진이 실제랑 비슷해요.", "sweet", "리뷰 남겨주셔서 감사해요!")
		);
	}

	private static List<DemoReviewSeed> pastaReviews() {
		return List.of(
				review(5, "알리오 올리오 마늘 향이 좋아요.", "salty", null),
				review(4, "뇨끼가 쫄깃해요. 크림 소스가 진해요.", "umami", null),
				review(5, "피자 도우가 얇고 바삭해서 만족!", "sweet", "다음에도 환영합니다 :)"),
				review(4, "데이트하기 좋은 분위기예요.", "sweet", null),
				review(4, "양이 넉넉하고 플레이팅이 예뻐요.", "umami", null),
				review(5, "강남역에서 이 정도 파스타면 충분히 재방문!", "salty", null)
		);
	}

	private static List<DemoReviewSeed> kimbapReviews() {
		return List.of(
				review(5, "참치김밥은 여기가 제일 맛있어요.", "salty", null),
				review(4, "라볶이 매콤달콤 딱 좋아요.", "sweet", null),
				review(4, "잔치국수 국물이 시원해요.", "sour", "자주 찾아주세요!"),
				review(5, "가성비 최고, 포장도 깔끔합니다.", "salty", null),
				review(4, "바쁜 점심에 빠르게 먹기 좋아요.", "umami", null),
				review(4, "김밥 속 재료가 신선해요.", "sweet", null)
		);
	}

	private static List<DemoReviewSeed> burgerReviews() {
		return List.of(
				review(5, "패티 육즙이 살아있어요. 번도 부드러워요.", "umami", null),
				review(4, "감자튀김 바삭하고 짜지 않아요.", "salty", null),
				review(5, "더블 치즈버거 치즈가 듬뿍!", "umami", "치즈 덕후 환영합니다"),
				review(4, "포장 상태도 좋았어요.", "salty", null),
				review(4, "양이 많아서 배부르게 먹었습니다.", "sweet", null),
				review(5, "강남역 버거 맛집으로 추천!", "umami", null)
		);
	}

	private static List<DemoReviewSeed> dessertReviews() {
		return List.of(
				review(5, "크로플 바삭하고 아이스크림과 찰떡!", "sweet", null),
				review(4, "말차 라떼 향이 진해요.", "bitter", null),
				review(5, "치즈케이크가 부드럽고 달지 않아요.", "sweet", "디저트는 매일 준비해요!"),
				review(4, "인스타 감성 카페 분위기.", "sweet", null),
				review(4, "커피랑 디저트 세트 가성비 좋아요.", "sweet", null),
				review(5, "데이트 코스로 또 올게요.", "sweet", null)
		);
	}

	private static DemoReviewSeed review(int rating, String content, String primaryTaste, String ownerReply) {
		return new DemoReviewSeed(rating, content, primaryTaste, ownerReply);
	}

	public record DemoLocationPreset(
			String id,
			String label,
			double lat,
			double lng,
			double radiusKm,
			boolean recommended
	) {
	}

	public record DemoStoreSeed(
			String name,
			String address,
			String addressDetail,
			double latitude,
			double longitude,
			int minOrderAmount,
			LocalTime openTime,
			LocalTime closeTime,
			int requiredTimeMinutes,
			List<String> storeImageUrls,
			List<DemoMenuSeed> menus,
			List<DemoReviewSeed> reviews
	) {
	}

	public record DemoMenuSeed(String name, long price, String description, String imageUrl) {
	}

	/** primaryTaste: sweet | salty | sour | bitter | umami */
	public record DemoReviewSeed(int rating, String content, String primaryTaste, String ownerReply) {
	}
}

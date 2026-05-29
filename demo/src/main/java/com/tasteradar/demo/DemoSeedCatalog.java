package com.tasteradar.demo;

import java.time.LocalTime;
import java.util.List;

/**
 * 포트폴리오 데모용 강남역 주변 샘플 데이터 정의.
 */
public final class DemoSeedCatalog {

	public static final String STORE_NAME_PREFIX = "[데모] ";
	public static final String DEMO_OWNER_EMAIL = "demo-owner@taste-radar.portfolio";

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
						"강남 한식당",
						"서울 강남구 강남대로 지하 396",
						"강남역 2번 출구",
						37.4985,
						127.0280,
						List.of(
								menu("한식 정식", 12000, "제철 반찬 6가지"),
								menu("된장찌개 단품", 9000, "국내산 두부"),
								menu("불고기 덮밥", 11000, "달짝한 양념")
						)
				),
				store(
						"강남 파스타",
						"서울 강남구 역삼동 테헤란로 152",
						"강남역 인근",
						37.4968,
						127.0255,
						List.of(
								menu("알리오 올리오", 13000, "올리브 오일 파스타"),
								menu("크림 뇨끼", 14000, "수제 뇨끼"),
								menu("마르게리타 피자", 16000, "모짜렐라 치즈")
						)
				),
				store(
						"강남 김밥천국",
						"서울 강남구 강남대로 390",
						"지하상가 B1",
						37.4992,
						127.0301,
						List.of(
								menu("참치김밥", 4500, "인기 메뉴"),
								menu("라볶이", 5500, "매콤한 소스"),
								menu("잔치국수", 6000, "얼큰한 육수")
						)
				),
				store(
						"강남 버거",
						"서울 강남구 역삼동 강남대로 지하 396",
						"푸드코트 12호",
						37.4955,
						127.0268,
						List.of(
								menu("클래식 버거", 9800, "숙성 패티"),
								menu("감자튀김", 3500, "바삭하게"),
								menu("더블 치즈 버거", 11500, "체다 2장")
						)
				),
				store(
						"강남 디저트",
						"서울 강남구 강남대로 382",
						"1층",
						37.5001,
						127.0240,
						List.of(
								menu("크로플", 7500, "아이스크림 토핑"),
								menu("말차 라떼", 5500, "국내산 말차"),
								menu("치즈케이크", 6800, "뉴욕 스타일")
						)
				)
		);
	}

	private static DemoStoreSeed store(
			String nameSuffix,
			String address,
			String addressDetail,
			double lat,
			double lng,
			List<DemoMenuSeed> menus
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
				menus
		);
	}

	private static DemoMenuSeed menu(String name, long price, String description) {
		return new DemoMenuSeed(name, price, description);
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
			List<DemoMenuSeed> menus
	) {
	}

	public record DemoMenuSeed(String name, long price, String description) {
	}
}

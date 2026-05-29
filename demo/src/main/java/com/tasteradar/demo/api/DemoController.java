package com.tasteradar.demo.api;

import com.tasteradar.demo.DemoSeedCatalog;
import com.tasteradar.demo.api.dto.DemoInfoResponse;
import com.tasteradar.demo.api.dto.DemoLocationResponse;
import com.tasteradar.global.config.DemoProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

	private final DemoProperties demoProperties;

	@GetMapping
	public DemoInfoResponse info() {
		double radiusKm = demoProperties.resolvedDefaultRadiusKm();
		var locations = toResponses(DemoSeedCatalog.locations(radiusKm));
		var primary = locations.getFirst();
		return new DemoInfoResponse(
				"Taste Radar Demo",
				"샘플 가게·메뉴는 서울 강남역 일대 좌표 기준입니다. "
						+ "프론트에서 데모 위치 프리셋을 적용한 뒤 GET /api/stores/nearby 를 호출하세요.",
				radiusKm,
				"/api/stores/nearby?lat=" + primary.lat() + "&lng=" + primary.lng() + "&radiusKm=" + radiusKm,
				demoProperties.seedEnabled(),
				locations
		);
	}

	@GetMapping("/locations")
	public List<DemoLocationResponse> locations() {
		return toResponses(DemoSeedCatalog.locations(demoProperties.resolvedDefaultRadiusKm()));
	}

	private static List<DemoLocationResponse> toResponses(List<DemoSeedCatalog.DemoLocationPreset> presets) {
		return presets.stream()
				.map(p -> new DemoLocationResponse(
						p.id(),
						p.label(),
						p.lat(),
						p.lng(),
						p.radiusKm(),
						p.recommended()
				))
				.toList();
	}
}

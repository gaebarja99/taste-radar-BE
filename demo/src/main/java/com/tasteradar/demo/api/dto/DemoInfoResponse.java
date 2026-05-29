package com.tasteradar.demo.api.dto;

import java.util.List;

public record DemoInfoResponse(
		String title,
		String description,
		double defaultRadiusKm,
		String nearbyApiPath,
		boolean seedEnabled,
		List<DemoLocationResponse> locations
) {
}

package com.tasteradar.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.demo")
public record DemoProperties(
		boolean seedEnabled,
		boolean reset,
		double defaultRadiusKm
) {
	public double resolvedDefaultRadiusKm() {
		return defaultRadiusKm > 0 ? defaultRadiusKm : 3.0;
	}
}

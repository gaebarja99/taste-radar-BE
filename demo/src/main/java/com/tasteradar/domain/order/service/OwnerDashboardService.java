package com.tasteradar.domain.order.service;

import com.tasteradar.domain.order.api.dto.DailySalesPointDto;
import com.tasteradar.domain.order.api.dto.OwnerRatingSummaryResponse;
import com.tasteradar.domain.order.api.dto.WeeklySalesResponse;
import com.tasteradar.domain.order.repository.FoodOrderRepository;
import com.tasteradar.domain.review.repository.ReviewRepository;
import com.tasteradar.domain.store.repository.StoreRepository;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private final FoodOrderRepository foodOrderRepository;
	private final ReviewRepository reviewRepository;
	private final StoreRepository storeRepository;

	@Transactional(readOnly = true)
	public WeeklySalesResponse weeklySales(long ownerId) {
		LocalDate today = LocalDate.now(SEOUL);
		LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate weekEndExclusive = weekStart.plusDays(7);
		LocalDate lastWeekStart = weekStart.minusDays(7);

		Instant thisStart = weekStart.atStartOfDay(SEOUL).toInstant();
		Instant thisEnd = weekEndExclusive.atStartOfDay(SEOUL).toInstant();
		Instant lastStart = lastWeekStart.atStartOfDay(SEOUL).toInstant();
		Instant lastEnd = weekStart.atStartOfDay(SEOUL).toInstant();

		Map<LocalDate, DailySalesPointDto> byDate = new HashMap<>();
		for (Object[] row : foodOrderRepository.sumDailySalesByOwner(ownerId, thisStart, thisEnd)) {
			LocalDate date = toLocalDate(row[0]);
			long sales = toLong(row[1]);
			long count = toLong(row[2]);
			byDate.put(date, new DailySalesPointDto(date, sales, count));
		}

		List<DailySalesPointDto> days = new ArrayList<>(7);
		for (int i = 0; i < 7; i++) {
			LocalDate d = weekStart.plusDays(i);
			days.add(byDate.getOrDefault(d, new DailySalesPointDto(d, 0L, 0L)));
		}

		long thisWeekTotal = foodOrderRepository.sumSalesByOwner(ownerId, thisStart, thisEnd);
		long lastWeekTotal = foodOrderRepository.sumSalesByOwner(ownerId, lastStart, lastEnd);
		double changePercent = percentChange(lastWeekTotal, thisWeekTotal);

		return new WeeklySalesResponse(days, thisWeekTotal, lastWeekTotal, changePercent);
	}

	@Transactional(readOnly = true)
	public OwnerRatingSummaryResponse ratingSummary(long ownerId) {
		long storeCount = storeRepository.findByOwner_IdOrderByIdAsc(ownerId).size();
		long reviewCount = reviewRepository.countReviewsForOwner(ownerId);
		double averageRating = reviewCount > 0
				? reviewRepository.averageRatingForOwner(ownerId)
				: 0.0;
		return new OwnerRatingSummaryResponse(
				Math.round(averageRating * 10.0) / 10.0,
				reviewCount,
				storeCount,
				reviewRepository.countByRatingForOwner(ownerId, 5),
				reviewRepository.countByRatingForOwner(ownerId, 4),
				reviewRepository.countByRatingForOwner(ownerId, 3),
				reviewRepository.countLowRatingForOwner(ownerId)
		);
	}

	private static double percentChange(long previous, long current) {
		if (previous <= 0) {
			return current > 0 ? 100.0 : 0.0;
		}
		return Math.round(((current - previous) / (double) previous) * 1000.0) / 10.0;
	}

	private static LocalDate toLocalDate(Object value) {
		if (value instanceof LocalDate ld) {
			return ld;
		}
		if (value instanceof Date sqlDate) {
			return sqlDate.toLocalDate();
		}
		if (value instanceof java.util.Date utilDate) {
			return utilDate.toInstant().atZone(SEOUL).toLocalDate();
		}
		return LocalDate.parse(String.valueOf(value));
	}

	private static long toLong(Object value) {
		if (value == null) {
			return 0L;
		}
		if (value instanceof Number n) {
			return n.longValue();
		}
		return Long.parseLong(String.valueOf(value));
	}
}

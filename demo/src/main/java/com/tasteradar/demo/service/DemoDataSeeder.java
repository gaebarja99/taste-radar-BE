package com.tasteradar.demo.service;

import com.tasteradar.demo.DemoSeedCatalog;
import com.tasteradar.demo.DemoSeedCatalog.DemoMenuSeed;
import com.tasteradar.demo.DemoSeedCatalog.DemoReviewSeed;
import com.tasteradar.demo.DemoSeedCatalog.DemoStoreSeed;
import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.order.entity.FoodOrder;
import com.tasteradar.domain.order.entity.OrderItem;
import com.tasteradar.domain.order.entity.OrderStatus;
import com.tasteradar.domain.order.repository.FoodOrderRepository;
import com.tasteradar.domain.review.entity.Review;
import com.tasteradar.domain.review.entity.ReviewMenuTasteEntry;
import com.tasteradar.domain.review.entity.TasteType;
import com.tasteradar.domain.review.repository.ReviewRepository;
import com.tasteradar.domain.store.entity.Store;
import com.tasteradar.domain.store.entity.StoreImage;
import com.tasteradar.domain.store.entity.StoreStatus;
import com.tasteradar.domain.store.repository.StoreRepository;
import com.tasteradar.domain.user.entity.User;
import com.tasteradar.domain.user.entity.UserRole;
import com.tasteradar.domain.user.repository.UserRepository;
import com.tasteradar.global.config.DemoProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.demo", name = "seed-enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

	private static final AtomicInteger ORDER_SEQ = new AtomicInteger(1);

	private final DemoProperties demoProperties;
	private final DemoStoreDataCleaner demoStoreDataCleaner;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;
	private final FoodOrderRepository foodOrderRepository;
	private final ReviewRepository reviewRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		boolean reset = isResetEnabled();
		log.info("[demo-seed] reset flag = {} (env DEMO_SEED_RESET={}, app.demo.reset={})",
				reset,
				System.getenv("DEMO_SEED_RESET"),
				demoProperties.reset());

		if (reset) {
			clearDemoData();
		} else if (storeRepository.existsByNameStartingWith(DemoSeedCatalog.STORE_NAME_PREFIX)) {
			log.info("[demo-seed] skip — demo stores already exist (set DEMO_SEED_RESET=true to reseed with images/reviews)");
			return;
		}

		User owner = userRepository.findByEmail(DemoSeedCatalog.DEMO_OWNER_EMAIL)
				.orElseGet(this::createDemoOwner);
		List<User> customers = ensureDemoCustomers(6);

		int storeCount = 0;
		int menuCount = 0;
		int reviewCount = 0;
		for (DemoStoreSeed seed : DemoSeedCatalog.stores()) {
			Store store = createStore(owner, seed);
			storeCount++;
			List<Menu> menus = new ArrayList<>();
			for (DemoMenuSeed menuSeed : seed.menus()) {
				menus.add(menuRepository.save(createMenu(store, menuSeed)));
				menuCount++;
			}
			reviewCount += seedReviews(store, menus, customers, seed.reviews());
			refreshStoreReviewStats(store.getId());
		}
		log.info(
				"[demo-seed] inserted {} stores, {} menus, {} reviews near Gangnam (lat={}, lng={}, radiusKm={})",
				storeCount,
				menuCount,
				reviewCount,
				DemoSeedCatalog.GANGNAM_STATION_LAT,
				DemoSeedCatalog.GANGNAM_STATION_LNG,
				demoProperties.resolvedDefaultRadiusKm()
		);
	}

	/** systemd EnvironmentFile(DEMO_SEED_RESET) + application.yml(app.demo.reset) 모두 인식 */
	private boolean isResetEnabled() {
		String env = System.getenv("DEMO_SEED_RESET");
		if (env != null && !env.isBlank()) {
			return parseBooleanLoose(env);
		}
		String appEnv = System.getenv("APP_DEMO_RESET");
		if (appEnv != null && !appEnv.isBlank()) {
			return parseBooleanLoose(appEnv);
		}
		return demoProperties.reset();
	}

	private static boolean parseBooleanLoose(String value) {
		String v = value.trim().toLowerCase();
		return v.equals("true") || v.equals("1") || v.equals("yes");
	}

	private void clearDemoData() {
		int clearedStores = demoStoreDataCleaner.hardDeleteAllDemoStores();
		if (clearedStores == 0) {
			return;
		}
		for (int i = 1; i <= 12; i++) {
			userRepository.findByEmail(DemoSeedCatalog.DEMO_CUSTOMER_EMAIL_PREFIX + i + "@taste-radar.portfolio")
					.ifPresent(userRepository::delete);
		}
		log.info("[demo-seed] cleared {} demo stores and related data (hard delete for reset)", clearedStores);
	}

	private User createDemoOwner() {
		User owner = new User();
		owner.setEmail(DemoSeedCatalog.DEMO_OWNER_EMAIL);
		owner.setNickname("데모사장");
		owner.setRole(UserRole.OWNER);
		owner.setDeleted(false);
		return userRepository.save(owner);
	}

	private List<User> ensureDemoCustomers(int count) {
		List<User> customers = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			final int customerNo = i;
			String email = DemoSeedCatalog.DEMO_CUSTOMER_EMAIL_PREFIX + customerNo + "@taste-radar.portfolio";
			User customer = userRepository.findByEmail(email).orElseGet(() -> {
				User u = new User();
				u.setEmail(email);
				u.setNickname("데모고객" + customerNo);
				u.setRole(UserRole.CUSTOMER);
				u.setDeleted(false);
				u.setZipCode("06234");
				u.setAddress("서울 강남구 강남대로");
				u.setAddressDetail("데모");
				return userRepository.save(u);
			});
			customers.add(customer);
		}
		return customers;
	}

	private Store createStore(User owner, DemoStoreSeed seed) {
		Store store = new Store();
		store.setOwner(owner);
		store.setName(seed.name());
		store.setAddress(seed.address());
		store.setAddressDetail(seed.addressDetail());
		store.setLatitude(seed.latitude());
		store.setLongitude(seed.longitude());
		store.setMinOrderAmount(seed.minOrderAmount());
		store.setOpenTime(seed.openTime());
		store.setCloseTime(seed.closeTime());
		store.setRequiredTimeMinutes(seed.requiredTimeMinutes());
		store.setStoreStatus(StoreStatus.OPEN);
		store.setDeleted(false);
		store.setAverageRating(0);
		store.setReviewCount(0);
		for (int i = 0; i < seed.storeImageUrls().size(); i++) {
			String url = seed.storeImageUrls().get(i);
			StoreImage image = new StoreImage();
			image.setStore(store);
			image.setFileName("demo-store-" + (i + 1) + ".jpg");
			image.setImgUrl(url);
			image.setImgKey("demo/" + store.getName() + "/" + (i + 1));
			store.getImages().add(image);
		}
		return storeRepository.save(store);
	}

	private Menu createMenu(Store store, DemoMenuSeed seed) {
		Menu menu = new Menu();
		menu.setStore(store);
		menu.setName(seed.name());
		menu.setPrice(seed.price());
		menu.setMenuDescription(seed.description());
		menu.setImageUrl(seed.imageUrl());
		menu.setDeleted(false);
		return menu;
	}

	private int seedReviews(Store store, List<Menu> menus, List<User> customers, List<DemoReviewSeed> templates) {
		if (menus.isEmpty() || templates.isEmpty()) {
			return 0;
		}
		Menu primaryMenu = menus.getFirst();
		int created = 0;
		for (int i = 0; i < templates.size(); i++) {
			DemoReviewSeed template = templates.get(i);
			User customer = customers.get(i % customers.size());
			FoodOrder order = createDeliveredOrder(store, customer, primaryMenu);
			reviewRepository.save(createReview(order, customer, primaryMenu, template));
			created++;
		}
		return created;
	}

	private FoodOrder createDeliveredOrder(Store store, User customer, Menu menu) {
		FoodOrder order = new FoodOrder();
		order.setUser(customer);
		order.setStore(store);
		order.setOrderNumber("DEMO-" + String.format("%05d", ORDER_SEQ.getAndIncrement()));
		order.setZipCode(customer.getZipCode() != null ? customer.getZipCode() : "06234");
		order.setAddress(customer.getAddress() != null ? customer.getAddress() : store.getAddress());
		order.setAddressDetail(customer.getAddressDetail() != null ? customer.getAddressDetail() : "101");
		order.setOrderStatus(OrderStatus.DELIVERED);
		order.setTotalAmount((int) menu.getPrice());

		OrderItem item = new OrderItem();
		item.setOrder(order);
		item.setMenu(menu);
		item.setMenuName(menu.getName());
		item.setQuantity(1);
		item.setPrice((int) menu.getPrice());
		order.getItems().add(item);

		return foodOrderRepository.save(order);
	}

	private Review createReview(FoodOrder order, User customer, Menu menu, DemoReviewSeed template) {
		TasteType taste = TasteType.fromApiKey(template.primaryTaste());
		Review review = new Review();
		review.setOrder(order);
		review.setUser(customer);
		review.setRating(template.rating());
		review.setContent(template.content());
		review.setDeleted(false);
		review.setOwnerReply(template.ownerReply());
		applyTasteFlags(review, taste);
		review.setMenuTastes(List.of(
				new ReviewMenuTasteEntry(menu.getId(), menu.getName(), taste.toApiKey())
		));
		return review;
	}

	private void applyTasteFlags(Review review, TasteType taste) {
		review.setSweetness(taste == TasteType.SWEET);
		review.setSaltiness(taste == TasteType.SALTY);
		review.setSourness(taste == TasteType.SOUR);
		review.setBitterness(taste == TasteType.BITTER);
		review.setUmami(taste == TasteType.UMAMI);
	}

	private void refreshStoreReviewStats(long storeId) {
		Store store = storeRepository.findById(storeId).orElseThrow();
		store.setReviewCount(reviewRepository.countReviewsForStore(storeId));
		store.setAverageRating(reviewRepository.averageRatingForStore(storeId));
	}
}

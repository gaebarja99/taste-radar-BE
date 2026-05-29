package com.tasteradar.demo.service;

import com.tasteradar.demo.DemoSeedCatalog;
import com.tasteradar.demo.DemoSeedCatalog.DemoMenuSeed;
import com.tasteradar.demo.DemoSeedCatalog.DemoStoreSeed;
import com.tasteradar.domain.menu.entity.Menu;
import com.tasteradar.domain.menu.repository.MenuRepository;
import com.tasteradar.domain.store.entity.Store;
import com.tasteradar.domain.store.entity.StoreStatus;
import com.tasteradar.domain.store.repository.StoreRepository;
import com.tasteradar.domain.user.entity.User;
import com.tasteradar.domain.user.entity.UserRole;
import com.tasteradar.domain.user.repository.UserRepository;
import com.tasteradar.global.config.DemoProperties;
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

	private static final String MENU_PLACEHOLDER_IMAGE =
			"https://placehold.co/400x300/png?text=Taste+Radar+Demo";

	private final DemoProperties demoProperties;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (storeRepository.existsByNameStartingWith(DemoSeedCatalog.STORE_NAME_PREFIX)) {
			log.info("[demo-seed] skip — demo stores already exist");
			return;
		}
		User owner = userRepository.findByEmail(DemoSeedCatalog.DEMO_OWNER_EMAIL)
				.orElseGet(this::createDemoOwner);
		int storeCount = 0;
		int menuCount = 0;
		for (DemoStoreSeed seed : DemoSeedCatalog.stores()) {
			Store store = createStore(owner, seed);
			storeCount++;
			for (DemoMenuSeed menuSeed : seed.menus()) {
				menuRepository.save(createMenu(store, menuSeed));
				menuCount++;
			}
		}
		log.info(
				"[demo-seed] inserted {} stores, {} menus near Gangnam (lat={}, lng={}, radiusKm={})",
				storeCount,
				menuCount,
				DemoSeedCatalog.GANGNAM_STATION_LAT,
				DemoSeedCatalog.GANGNAM_STATION_LNG,
				demoProperties.resolvedDefaultRadiusKm()
		);
	}

	private User createDemoOwner() {
		User owner = new User();
		owner.setEmail(DemoSeedCatalog.DEMO_OWNER_EMAIL);
		owner.setNickname("데모사장");
		owner.setRole(UserRole.OWNER);
		owner.setDeleted(false);
		return userRepository.save(owner);
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
		store.setAverageRating(4.5);
		store.setReviewCount(12);
		return storeRepository.save(store);
	}

	private Menu createMenu(Store store, DemoMenuSeed seed) {
		Menu menu = new Menu();
		menu.setStore(store);
		menu.setName(seed.name());
		menu.setPrice(seed.price());
		menu.setMenuDescription(seed.description());
		menu.setImageUrl(MENU_PLACEHOLDER_IMAGE);
		menu.setDeleted(false);
		return menu;
	}
}

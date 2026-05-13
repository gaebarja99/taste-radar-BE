-- ============================================================
-- Taste Radar — 가게 4~15 DELIVERED 주문 + 리뷰 일괄 시드
-- MySQL 8+
--
-- users  : id, created_at, updated_at, email, is_deleted, nickname, role
-- orders : id, created_at, updated_at, address, address_detail, order_number,
--          order_status, rejection_reason, total_amount, zip_code, store_id, user_id
-- reviews: created_at, updated_at, bitterness, content, is_deleted, owner_reply,
--          rating, saltiness, sourness, sweetness, umami, order_id, user_id
--
-- 가게별 리뷰(=주문) 개수: 4 또는 5 (총 54건)
-- order_number 접두사: SEED-20260513-  (재실행 시 중복 방지)
-- ============================================================

-- 0) 사전 확인
SELECT id, name, review_count FROM stores WHERE id BETWEEN 4 AND 15 ORDER BY id;

SELECT id, nickname, role
FROM users
WHERE role = 'CUSTOMER' AND is_deleted = 0
ORDER BY id
LIMIT 20;

-- ============================================================
-- 1) 본문
-- ============================================================

START TRANSACTION;

-- connection(utf8mb4_0900_ai_ci) vs 테이블(utf8mb4_unicode_ci) 충돌 방지
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 1-1) CUSTOMER가 없으면 시드 고객 8명 생성
INSERT INTO users (created_at, updated_at, email, is_deleted, nickname, role)
SELECT NOW(6), NOW(6), v.email, 0, v.nickname, 'CUSTOMER'
FROM (
    SELECT 'seed.c01@taste-radar.local' AS email, '시드고객1' AS nickname UNION ALL
    SELECT 'seed.c02@taste-radar.local', '시드고객2' UNION ALL
    SELECT 'seed.c03@taste-radar.local', '시드고객3' UNION ALL
    SELECT 'seed.c04@taste-radar.local', '시드고객4' UNION ALL
    SELECT 'seed.c05@taste-radar.local', '시드고객5' UNION ALL
    SELECT 'seed.c06@taste-radar.local', '시드고객6' UNION ALL
    SELECT 'seed.c07@taste-radar.local', '시드고객7' UNION ALL
    SELECT 'seed.c08@taste-radar.local', '시드고객8'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE BINARY u.email = BINARY v.email
);

DROP TEMPORARY TABLE IF EXISTS tmp_store_quota;
CREATE TEMPORARY TABLE tmp_store_quota (
    store_id    BIGINT PRIMARY KEY,
    max_reviews INT NOT NULL
);

INSERT INTO tmp_store_quota (store_id, max_reviews) VALUES
(4,  5), (5,  4), (6,  5), (7,  4), (8,  5), (9,  4),
(10, 5), (11, 4), (12, 5), (13, 4), (14, 5), (15, 4);

DROP TEMPORARY TABLE IF EXISTS tmp_customer_pool;
CREATE TEMPORARY TABLE tmp_customer_pool AS
SELECT
    id AS user_id,
    ROW_NUMBER() OVER (ORDER BY id) AS rn,
    COUNT(*) OVER ()                AS total
FROM users
WHERE BINARY role = BINARY 'CUSTOMER'
  AND is_deleted = 0;

-- CUSTOMER가 0명이면 여기서 실패 → 위 INSERT 확인
DROP TEMPORARY TABLE IF EXISTS tmp_seed_slots;
CREATE TEMPORARY TABLE tmp_seed_slots AS
SELECT
    q.store_id,
    s.slot,
    q.max_reviews,
    CONCAT('SEED-20260513-S', LPAD(q.store_id, 2, '0'), '-', LPAD(s.slot, 2, '0'))
        COLLATE utf8mb4_unicode_ci AS order_number,
    cp.user_id,
    GREATEST(st.min_order_amount, 12000) AS total_amount
FROM tmp_store_quota q
JOIN (
    SELECT 1 AS slot UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) s ON s.slot <= q.max_reviews
JOIN stores st ON st.id = q.store_id
JOIN tmp_customer_pool cp
  ON cp.rn = ((q.store_id * 10 + s.slot) MOD cp.total) + 1;

-- 1-2) DELIVERED 주문 INSERT
INSERT INTO orders (
    created_at, updated_at,
    address, address_detail, order_number,
    order_status, rejection_reason, total_amount,
    zip_code, store_id, user_id
)
SELECT
    DATE_SUB(NOW(6), INTERVAL (p.store_id * 7 + p.slot) DAY),
    DATE_SUB(NOW(6), INTERVAL (p.store_id * 7 + p.slot) DAY),
    '서울특별시 강남구 테헤란로 123',
    CONCAT(p.store_id, '호'),
    p.order_number,
    'DELIVERED',
    NULL,
    p.total_amount,
    '06236',
    p.store_id,
    p.user_id
FROM tmp_seed_slots p
WHERE NOT EXISTS (
    SELECT 1 FROM orders o WHERE BINARY o.order_number = BINARY p.order_number
);

-- 1-3) 리뷰 템플릿
DROP TEMPORARY TABLE IF EXISTS tmp_review_templates;
CREATE TEMPORARY TABLE tmp_review_templates (
    store_id   BIGINT NOT NULL,
    slot       INT    NOT NULL,
    content    TEXT   NOT NULL,
    rating     INT    NOT NULL,
    sweetness  INT    NOT NULL,
    saltiness  INT    NOT NULL,
    sourness   INT    NOT NULL,
    bitterness INT    NOT NULL,
    umami      INT    NOT NULL,
    PRIMARY KEY (store_id, slot)
);

INSERT INTO tmp_review_templates
    (store_id, slot, content, rating, sweetness, saltiness, sourness, bitterness, umami)
VALUES
(4, 1, '양념이 진하고 고소해서 술안주로 딱이에요.', 5, 2, 4, 2, 1, 5),
(4, 2, '바삭함은 좋은데 조금 짰어요. 밥이랑 먹으면 괜찮아요.', 4, 2, 4, 1, 1, 4),
(4, 3, '감칠맛이 확실해서 재주문 의사 있어요.', 5, 2, 3, 2, 1, 5),
(4, 4, '기대 이상으로 맛있었습니다. 포장도 깔끔했어요.', 5, 3, 3, 2, 1, 4),
(4, 5, '전체적으로 간이 세지만 만족스러워요.', 4, 2, 4, 1, 2, 4),
(5, 1, '디저트가 달달하고 부드러워요. 커피랑 잘 어울립니다.', 5, 5, 1, 2, 1, 2),
(5, 2, '단맛이 과하지 않아서 좋았어요.', 4, 4, 1, 2, 1, 2),
(5, 3, '비주얼도 예쁘고 맛도 만족!', 5, 5, 1, 1, 1, 2),
(5, 4, '달콤한 맛이 일품이에요. 다음에 또 올게요.', 5, 5, 1, 2, 1, 3),
(6, 1, '국물이 시원하고 깔끔해요. 해장용으로 좋습니다.', 5, 1, 3, 4, 1, 3),
(6, 2, '신맛이 적당해서 느끼하지 않아요.', 4, 2, 3, 4, 1, 3),
(6, 3, '재료가 신선하고 양도 넉넉했어요.', 5, 2, 3, 3, 1, 3),
(6, 4, '짠맛이 조금 있지만 전반적으로 만족합니다.', 4, 2, 4, 3, 1, 3),
(6, 5, '시원한 맛이 입맛 돋워요.', 5, 1, 3, 5, 1, 2),
(7, 1, '전반적으로 균형 잡힌 맛이에요.', 4, 3, 3, 3, 2, 3),
(7, 2, '가격 대비 퀄리티 좋습니다.', 4, 3, 3, 2, 2, 3),
(7, 3, '맛있게 잘 먹었어요. 배달도 빨랐습니다.', 5, 3, 3, 2, 2, 3),
(7, 4, '무난하게 맛있는 편이에요.', 4, 3, 3, 3, 2, 3),
(8, 1, '육수가 진하고 깊은 맛이 나요.', 5, 2, 4, 1, 1, 5),
(8, 2, '면이 쫄깃하고 국물이 찐해요.', 5, 2, 4, 1, 1, 5),
(8, 3, '감칠맛 최고입니다. 자주 시킬 것 같아요.', 5, 2, 3, 1, 1, 5),
(8, 4, '조금 짠 편이지만 맛은 확실해요.', 4, 2, 4, 1, 2, 4),
(8, 5, '뜨끈하게 와서 만족했습니다.', 5, 2, 3, 2, 1, 4),
(9, 1, '빵이 부드럽고 달콤해요.', 5, 4, 2, 1, 1, 2),
(9, 2, '단맛이 은은해서 좋았습니다.', 4, 4, 2, 1, 1, 2),
(9, 3, '향이 좋고 달지 않게 잘 나왔어요.', 5, 4, 2, 1, 2, 2),
(9, 4, '디저트 맛집 인정!', 5, 5, 1, 1, 1, 2),
(10, 1, '간이 세서 밥이랑 먹기 좋아요.', 4, 2, 5, 1, 1, 4),
(10, 2, '짠맛 좋아하시면 추천합니다.', 5, 2, 5, 1, 1, 4),
(10, 3, '양념이 진해서 맛있었어요.', 4, 2, 4, 2, 1, 4),
(10, 4, '조금 짰지만 그게 매력이에요.', 4, 2, 5, 1, 2, 3),
(10, 5, '재주문했어요. 역시 짭짤합니다.', 5, 2, 4, 1, 1, 4),
(11, 1, '새콤달콤해서 입맛 돋아요.', 5, 3, 2, 5, 1, 2),
(11, 2, '신맛이 상큼하고 좋습니다.', 4, 3, 2, 4, 1, 2),
(11, 3, '냉면처럼 시원한 맛이에요.', 5, 2, 2, 5, 1, 2),
(11, 4, '새콤한 양념이 일품입니다.', 5, 3, 2, 4, 1, 3),
(12, 1, '원두 향이 좋고 쓴맛이 깔끔해요.', 5, 1, 1, 2, 4, 3),
(12, 2, '에스프레소 바디감이 좋습니다.', 4, 1, 1, 2, 4, 3),
(12, 3, '쓴맛과 고소함의 밸런스가 좋아요.', 5, 2, 1, 2, 3, 3),
(12, 4, '커피 맛집 맞네요. 산미도 적당해요.', 5, 2, 1, 3, 3, 2),
(12, 5, '진한 맛이 좋았습니다.', 4, 1, 1, 2, 4, 3),
(13, 1, '맛있게 잘 먹었습니다.', 4, 3, 3, 3, 2, 3),
(13, 2, '배달 빠르고 음식도 따뜻했어요.', 5, 3, 3, 2, 2, 3),
(13, 3, '무난하지만 만족스러운 한 끼였어요.', 4, 3, 3, 3, 2, 3),
(13, 4, '다음에도 주문할 의향 있어요.', 4, 3, 3, 2, 2, 4),
(14, 1, '육즙이 살아있고 감칠맛이 풍부해요.', 5, 2, 3, 1, 1, 5),
(14, 2, '고기 질이 좋고 맛이 깊습니다.', 5, 2, 3, 1, 1, 5),
(14, 3, '양념보다 고기 본연의 맛이 좋아요.', 5, 2, 3, 1, 2, 5),
(14, 4, '감칠맛 덕분에 밥도둑이에요.', 5, 2, 3, 1, 1, 5),
(14, 5, '가격 대비 만족도 높습니다.', 4, 2, 3, 2, 1, 4),
(15, 1, '달콤하면서도 짭짤한 맛이 조화로워요.', 5, 4, 3, 2, 1, 3),
(15, 2, '간장 베이스가 맛있습니다.', 4, 3, 4, 2, 1, 3),
(15, 3, '단짠 조합이 중독성 있어요.', 5, 4, 3, 1, 1, 4),
(15, 4, '전체적으로 맛있게 잘 먹었어요.', 4, 3, 3, 2, 1, 3);

-- 1-4) 리뷰 INSERT (방금 넣은 시드 주문에 연결)
INSERT INTO reviews (
    created_at, updated_at,
    bitterness, content, is_deleted, owner_reply,
    rating, saltiness, sourness, sweetness, umami,
    order_id, user_id
)
SELECT
    o.created_at,
    o.updated_at,
    t.bitterness,
    t.content,
    0,
    NULL,
    t.rating,
    t.saltiness,
    t.sourness,
    t.sweetness,
    t.umami,
    o.id,
    o.user_id
FROM orders o
JOIN tmp_seed_slots p
  ON BINARY p.order_number = BINARY o.order_number
JOIN tmp_review_templates t
  ON t.store_id = p.store_id
 AND t.slot     = p.slot
WHERE NOT EXISTS (
    SELECT 1 FROM reviews r WHERE r.order_id = o.id
);

-- 1-5) 가게 평점·리뷰 수 갱신
UPDATE stores s
SET
    review_count = (
        SELECT COUNT(*)
        FROM reviews r
        JOIN orders o ON o.id = r.order_id
        WHERE o.store_id = s.id
          AND r.is_deleted = 0
    ),
    average_rating = (
        SELECT COALESCE(AVG(r.rating), 0)
        FROM reviews r
        JOIN orders o ON o.id = r.order_id
        WHERE o.store_id = s.id
          AND r.is_deleted = 0
    )
WHERE s.id BETWEEN 4 AND 15;

COMMIT;

-- ============================================================
-- 2) 결과 확인
-- ============================================================
SELECT
    s.id AS store_id,
    s.name,
    s.review_count,
    ROUND(s.average_rating, 1) AS average_rating,
    COUNT(DISTINCT o.id)       AS seed_orders,
    COUNT(r.id)                AS seed_reviews
FROM stores s
LEFT JOIN orders o
  ON o.store_id = s.id
 AND BINARY o.order_number LIKE BINARY 'SEED-20260513-%'
LEFT JOIN reviews r
  ON r.order_id = o.id
 AND r.is_deleted = 0
WHERE s.id BETWEEN 4 AND 15
GROUP BY s.id, s.name, s.review_count, s.average_rating
ORDER BY s.id;

SELECT
    s.id,
    ROUND(AVG(r.sweetness), 1)  AS avg_sweet,
    ROUND(AVG(r.saltiness), 1)  AS avg_salty,
    ROUND(AVG(r.sourness), 1)   AS avg_sour,
    ROUND(AVG(r.bitterness), 1) AS avg_bitter,
    ROUND(AVG(r.umami), 1)      AS avg_umami
FROM stores s
JOIN orders o ON o.store_id = s.id
JOIN reviews r ON r.order_id = o.id AND r.is_deleted = 0
WHERE s.id BETWEEN 4 AND 15
GROUP BY s.id
ORDER BY s.id;

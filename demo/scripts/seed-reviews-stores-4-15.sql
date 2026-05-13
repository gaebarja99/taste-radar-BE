-- ============================================================
-- Taste Radar — 가게 4~15 더미 리뷰 시드 (기존 DELIVERED 주문용)
--
-- ※ 주문이 없으면 아래 통합 스크립트를 사용하세요:
--    seed-orders-and-reviews-stores-4-15.sql
--
-- reviews 컬럼: id, created_at, updated_at, bitterness, content,
--               is_deleted, owner_reply, rating, saltiness, sourness,
--               sweetness, umami, order_id, user_id
--
-- 전제 조건
--   1) orders.store_id 가 4~15 인 DELIVERED 주문이 가게마다 4~5개 이상
--   2) 해당 주문에 아직 reviews 가 없음 (주문 1건당 리뷰 1건)
--
-- 실행 전 확인 (0번) 결과에서 available_orders 가 quota 이상인지 본다.
-- ============================================================

-- 0) 가게별 사용 가능한 DELIVERED 주문 수 확인
SELECT
    q.store_id,
    q.max_reviews AS quota,
    COUNT(o.id)   AS available_orders
FROM (
    SELECT 4 AS store_id, 5 AS max_reviews UNION ALL
    SELECT 5, 4 UNION ALL SELECT 6, 5 UNION ALL SELECT 7, 4 UNION ALL
    SELECT 8, 5 UNION ALL SELECT 9, 4 UNION ALL SELECT 10, 5 UNION ALL
    SELECT 11, 4 UNION ALL SELECT 12, 5 UNION ALL SELECT 13, 4 UNION ALL
    SELECT 14, 5 UNION ALL SELECT 15, 4
) q
LEFT JOIN orders o
    ON o.store_id = q.store_id
   AND o.order_status = 'DELIVERED'
   AND NOT EXISTS (SELECT 1 FROM reviews r WHERE r.order_id = o.id)
GROUP BY q.store_id, q.max_reviews
ORDER BY q.store_id;

-- ============================================================
-- 1) 시드 본문 (MySQL 8+)
-- ============================================================

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_store_review_quota;
CREATE TEMPORARY TABLE tmp_store_review_quota (
    store_id    BIGINT PRIMARY KEY,
    max_reviews INT NOT NULL
);

INSERT INTO tmp_store_review_quota (store_id, max_reviews) VALUES
(4,  5), (5,  4), (6,  5), (7,  4), (8,  5), (9,  4),
(10, 5), (11, 4), (12, 5), (13, 4), (14, 5), (15, 4);

DROP TEMPORARY TABLE IF EXISTS tmp_order_slots;
CREATE TEMPORARY TABLE tmp_order_slots AS
SELECT
    o.id      AS order_id,
    o.user_id AS user_id,
    o.store_id,
    q.max_reviews,
    ROW_NUMBER() OVER (PARTITION BY o.store_id ORDER BY o.id) AS slot
FROM orders o
JOIN tmp_store_review_quota q ON q.store_id = o.store_id
WHERE o.order_status = 'DELIVERED'
  AND NOT EXISTS (SELECT 1 FROM reviews r WHERE r.order_id = o.id);

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
-- store 4 (5) — 짠맛·감칠맛 위주
(4, 1, '양념이 진하고 고소해서 술안주로 딱이에요.', 5, 2, 4, 2, 1, 5),
(4, 2, '바삭함은 좋은데 조금 짰어요. 밥이랑 먹으면 괜찮아요.', 4, 2, 4, 1, 1, 4),
(4, 3, '감칠맛이 확실해서 재주문 의사 있어요.', 5, 2, 3, 2, 1, 5),
(4, 4, '기대 이상으로 맛있었습니다. 포장도 깔끔했어요.', 5, 3, 3, 2, 1, 4),
(4, 5, '전체적으로 간이 세지만 만족스러워요.', 4, 2, 4, 1, 2, 4),

-- store 5 (4) — 단맛 위주
(5, 1, '디저트가 달달하고 부드러워요. 커피랑 잘 어울립니다.', 5, 5, 1, 2, 1, 2),
(5, 2, '단맛이 과하지 않아서 좋았어요.', 4, 4, 1, 2, 1, 2),
(5, 3, '비주얼도 예쁘고 맛도 만족!', 5, 5, 1, 1, 1, 2),
(5, 4, '달콤한 맛이 일품이에요. 다음에 또 올게요.', 5, 5, 1, 2, 1, 3),

-- store 6 (5) — 신맛·짠맛
(6, 1, '국물이 시원하고 깔끔해요. 해장용으로 좋습니다.', 5, 1, 3, 4, 1, 3),
(6, 2, '신맛이 적당해서 느끼하지 않아요.', 4, 2, 3, 4, 1, 3),
(6, 3, '재료가 신선하고 양도 넉넉했어요.', 5, 2, 3, 3, 1, 3),
(6, 4, '짠맛이 조금 있지만 전반적으로 만족합니다.', 4, 2, 4, 3, 1, 3),
(6, 5, '시원한 맛이 입맛 돋워요.', 5, 1, 3, 5, 1, 2),

-- store 7 (4) — 밸런스
(7, 1, '전반적으로 균형 잡힌 맛이에요.', 4, 3, 3, 3, 2, 3),
(7, 2, '가격 대비 퀄리티 좋습니다.', 4, 3, 3, 2, 2, 3),
(7, 3, '맛있게 잘 먹었어요. 배달도 빨랐습니다.', 5, 3, 3, 2, 2, 3),
(7, 4, '무난하게 맛있는 편이에요.', 4, 3, 3, 3, 2, 3),

-- store 8 (5) — 감칠맛·짠맛
(8, 1, '육수가 진하고 깊은 맛이 나요.', 5, 2, 4, 1, 1, 5),
(8, 2, '면이 쫄깃하고 국물이 찐해요.', 5, 2, 4, 1, 1, 5),
(8, 3, '감칠맛 최고입니다. 자주 시킬 것 같아요.', 5, 2, 3, 1, 1, 5),
(8, 4, '조금 짠 편이지만 맛은 확실해요.', 4, 2, 4, 1, 2, 4),
(8, 5, '뜨끈하게 와서 만족했습니다.', 5, 2, 3, 2, 1, 4),

-- store 9 (4) — 단맛
(9, 1, '빵이 부드럽고 달콤해요.', 5, 4, 2, 1, 1, 2),
(9, 2, '단맛이 은은해서 좋았습니다.', 4, 4, 2, 1, 1, 2),
(9, 3, '향이 좋고 달지 않게 잘 나왔어요.', 5, 4, 2, 1, 2, 2),
(9, 4, '디저트 맛집 인정!', 5, 5, 1, 1, 1, 2),

-- store 10 (5) — 짠맛
(10, 1, '간이 세서 밥이랑 먹기 좋아요.', 4, 2, 5, 1, 1, 4),
(10, 2, '짠맛 좋아하시면 추천합니다.', 5, 2, 5, 1, 1, 4),
(10, 3, '양념이 진해서 맛있었어요.', 4, 2, 4, 2, 1, 4),
(10, 4, '조금 짰지만 그게 매력이에요.', 4, 2, 5, 1, 2, 3),
(10, 5, '재주문했어요. 역시 짭짤합니다.', 5, 2, 4, 1, 1, 4),

-- store 11 (4) — 신맛
(11, 1, '새콤달콤해서 입맛 돋아요.', 5, 3, 2, 5, 1, 2),
(11, 2, '신맛이 상큼하고 좋습니다.', 4, 3, 2, 4, 1, 2),
(11, 3, '냉면처럼 시원한 맛이에요.', 5, 2, 2, 5, 1, 2),
(11, 4, '새콤한 양념이 일품입니다.', 5, 3, 2, 4, 1, 3),

-- store 12 (5) — 쓴맛·감칠맛
(12, 1, '원두 향이 좋고 쓴맛이 깔끔해요.', 5, 1, 1, 2, 4, 3),
(12, 2, '에스프레소 바디감이 좋습니다.', 4, 1, 1, 2, 4, 3),
(12, 3, '쓴맛과 고소함의 밸런스가 좋아요.', 5, 2, 1, 2, 3, 3),
(12, 4, '커피 맛집 맞네요. 산미도 적당해요.', 5, 2, 1, 3, 3, 2),
(12, 5, '진한 맛이 좋았습니다.', 4, 1, 1, 2, 4, 3),

-- store 13 (4) — 혼합
(13, 1, '맛있게 잘 먹었습니다.', 4, 3, 3, 3, 2, 3),
(13, 2, '배달 빠르고 음식도 따뜻했어요.', 5, 3, 3, 2, 2, 3),
(13, 3, '무난하지만 만족스러운 한 끼였어요.', 4, 3, 3, 3, 2, 3),
(13, 4, '다음에도 주문할 의향 있어요.', 4, 3, 3, 2, 2, 4),

-- store 14 (5) — 감칠맛
(14, 1, '육즙이 살아있고 감칠맛이 풍부해요.', 5, 2, 3, 1, 1, 5),
(14, 2, '고기 질이 좋고 맛이 깊습니다.', 5, 2, 3, 1, 1, 5),
(14, 3, '양념보다 고기 본연의 맛이 좋아요.', 5, 2, 3, 1, 2, 5),
(14, 4, '감칠맛 덕분에 밥도둑이에요.', 5, 2, 3, 1, 1, 5),
(14, 5, '가격 대비 만족도 높습니다.', 4, 2, 3, 2, 1, 4),

-- store 15 (4) — 단맛·짠맛
(15, 1, '달콤하면서도 짭짤한 맛이 조화로워요.', 5, 4, 3, 2, 1, 3),
(15, 2, '간장 베이스가 맛있습니다.', 4, 3, 4, 2, 1, 3),
(15, 3, '단짠 조합이 중독성 있어요.', 5, 4, 3, 1, 1, 4),
(15, 4, '전체적으로 맛있게 잘 먹었어요.', 4, 3, 3, 2, 1, 3);

INSERT INTO reviews (
    created_at, updated_at,
    bitterness, content, is_deleted, owner_reply,
    rating, saltiness, sourness, sweetness, umami,
    order_id, user_id
)
SELECT
    DATE_SUB(NOW(6), INTERVAL (s.store_id * 7 + t.slot) DAY) AS created_at,
    DATE_SUB(NOW(6), INTERVAL (s.store_id * 7 + t.slot) DAY) AS updated_at,
    t.bitterness,
    t.content,
    0,
    NULL,
    t.rating,
    t.saltiness,
    t.sourness,
    t.sweetness,
    t.umami,
    s.order_id,
    s.user_id
FROM tmp_order_slots s
JOIN tmp_review_templates t
  ON t.store_id = s.store_id
 AND t.slot     = s.slot
WHERE s.slot <= s.max_reviews;

-- 가게 평점·리뷰 수 갱신 (앱 로직과 동일하게 집계)
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

-- 2) 결과 확인
SELECT
    s.id,
    s.name,
    s.review_count,
    ROUND(s.average_rating, 1) AS average_rating,
    ROUND(AVG(r.sweetness), 1) AS avg_sweet,
    ROUND(AVG(r.saltiness), 1) AS avg_salty,
    ROUND(AVG(r.sourness), 1)  AS avg_sour,
    ROUND(AVG(r.bitterness), 1) AS avg_bitter,
    ROUND(AVG(r.umami), 1)     AS avg_umami
FROM stores s
LEFT JOIN orders o ON o.store_id = s.id
LEFT JOIN reviews r ON r.order_id = o.id AND r.is_deleted = 0
WHERE s.id BETWEEN 4 AND 15
GROUP BY s.id, s.name, s.review_count, s.average_rating
ORDER BY s.id;

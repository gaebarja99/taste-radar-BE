-- 기존 리뷰 맛 점수(1~5)를 boolean 특화 맛(0/1)으로 변환
-- 3점 이상이면 해당 맛 '특화'로 간주

UPDATE reviews
SET
    sweetness  = IF(sweetness >= 3, 1, 0),
    saltiness  = IF(saltiness >= 3, 1, 0),
    sourness   = IF(sourness >= 3, 1, 0),
    bitterness = IF(bitterness >= 3, 1, 0),
    umami      = IF(umami >= 3, 1, 0);

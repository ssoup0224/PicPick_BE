-- 8. Initialize Mart Items
INSERT INTO
    mart_item (
        item_name,
        item_price,
        start_date,
        end_date,
        discount_percentage,
        mart_id
    )
VALUES (
        '샤인머스켓 2kg/1박스',
        8900,
        '2026-01-01',
        '2026-01-07',
        11,
        1
    ),
    (
        '킹스베리 딸기 500g',
        12900,
        '2026-01-01',
        '2026-01-07',
        15,
        1
    ),
    (
        '한우 등심 1+등급 300g',
        35000,
        '2026-01-01',
        '2026-01-07',
        20,
        1
    ),
    (
        '대파 1단',
        2500,
        '2026-01-01',
        '2026-01-07',
        0,
        1
    ),
    (
        '틈새라면 5개입',
        4500,
        '2026-01-01',
        '2026-01-07',
        5,
        1
    )
ON DUPLICATE KEY UPDATE
    item_price = VALUES(item_price),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    discount_percentage = VALUES(discount_percentage);
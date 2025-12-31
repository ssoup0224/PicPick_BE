-- Insert mock Mart data
INSERT INTO
    mart (
        name,
        address,
        latitude,
        longitude,
        created_at
    )
VALUES (
        'Yesod',
        'South Korea, North Gyeongsang Pohang-si Buk-gu Handong-ro 558, 37554',
        36.1026479542082,
        129.38652889624808,
        NOW()
    );
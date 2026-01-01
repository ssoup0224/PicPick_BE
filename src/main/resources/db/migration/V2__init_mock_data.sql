-- Create User mock data
INSERT INTO
    user (
        id,
        uuid,
        role,
        current_latitude,
        current_longitude,
        created_at,
        total_scans
    )
VALUES (
        1,
        'mock-user-uuid-1234',
        'USER',
        37.5665,
        126.9780,
        NOW(),
        29
    )
ON DUPLICATE KEY UPDATE
    uuid = uuid;

-- Create Mart mock data
INSERT INTO
    mart (
        id,
        name,
        address,
        latitude,
        longitude,
        created_at
    )
VALUES (
        1,
        'Mock Mart',
        'Seoul, South Korea',
        37.5665,
        126.9780,
        NOW()
    )
ON DUPLICATE KEY UPDATE
    name = name;
-- 1. Mart Table
CREATE TABLE mart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE,
    document_file VARCHAR(255),
    brn VARCHAR(50),
    created_at DATETIME(6)
);

-- 2. User Table
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50),
    created_at DATETIME(6),
    last_login DATETIME(6),
    total_scans INT DEFAULT 0,
    current_longitude DOUBLE,
    current_latitude DOUBLE,
    current_mart_id BIGINT,
    CONSTRAINT fk_user_current_mart FOREIGN KEY (current_mart_id) REFERENCES mart (id)
);

-- 3. Login Log Table
CREATE TABLE login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_at DATETIME(6) NOT NULL,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    CONSTRAINT fk_login_log_user FOREIGN KEY (user_id) REFERENCES user (id)
);

-- 4. Mart Item Table
CREATE TABLE mart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL UNIQUE,
    item_price INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    discount_percentage INT,
    mart_id BIGINT NOT NULL,
    CONSTRAINT fk_mart_item_mart FOREIGN KEY (mart_id) REFERENCES mart (id)
);

-- 5. Online Item Table
CREATE TABLE online_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    naver_product_id VARCHAR(255),
    item_name VARCHAR(255) NOT NULL UNIQUE,
    item_price INT NOT NULL,
    last_updated DATETIME(6)
);

-- 6. Scan Log Table
CREATE TABLE scan_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    description TEXT,
    scanned_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    mart_id BIGINT NOT NULL,
    CONSTRAINT fk_scan_log_user FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_scan_log_mart FOREIGN KEY (mart_id) REFERENCES mart (id)
);
-- Final database schema based on current entities

-- 1. mart table
CREATE TABLE mart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE,
    document_file VARCHAR(255),
    brn VARCHAR(255),
    created_at DATETIME
);

-- 2. user table
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50),
    created_at DATETIME,
    last_login DATETIME,
    total_scans INT DEFAULT 0,
    current_longitude DOUBLE,
    current_latitude DOUBLE,
    current_mart_id BIGINT,
    CONSTRAINT fk_user_current_mart FOREIGN KEY (current_mart_id) REFERENCES mart (id)
);

-- 3. mart_item table
CREATE TABLE mart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    item_price INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    discount_percentage INT,
    mart_id BIGINT NOT NULL,
    CONSTRAINT fk_mart_item_mart FOREIGN KEY (mart_id) REFERENCES mart (id),
    UNIQUE KEY uk_mart_item_name (mart_id, item_name)
);

-- 4. login_log table
CREATE TABLE login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_at DATETIME NOT NULL,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    CONSTRAINT fk_login_log_user FOREIGN KEY (user_id) REFERENCES user (id)
);

-- 5. online_item table
CREATE TABLE online_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    naver_product_id VARCHAR(255),
    item_brand VARCHAR(255),
    item_name VARCHAR(255) NOT NULL UNIQUE,
    item_price INT NOT NULL,
    last_updated DATETIME
);

-- 6. scan_log table
CREATE TABLE scan_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    description TEXT,
    scanned_at DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    mart_id BIGINT NOT NULL,
    online_item_id BIGINT,
    CONSTRAINT fk_scan_log_user FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT fk_scan_log_mart FOREIGN KEY (mart_id) REFERENCES mart (id),
    CONSTRAINT fk_scan_log_online_item FOREIGN KEY (online_item_id) REFERENCES online_item (id)
);

-- 7. analysis_report table
CREATE TABLE analysis_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255),
    chosen_category VARCHAR(255),
    pick_score DOUBLE,
    credibility_score DOUBLE,
    mart_price INT,
    online_price INT,
    price_difference_percent DOUBLE,
    index_one VARCHAR(255),
    index_two VARCHAR(255),
    index_three VARCHAR(255),
    index_four VARCHAR(255),
    index_five VARCHAR(255),
    quality_info TEXT,
    price_info TEXT,
    conclusion_info TEXT,
    scan_log_id BIGINT,
    CONSTRAINT fk_analysis_report_scan_log FOREIGN KEY (scan_log_id) REFERENCES scan_log (id)
);
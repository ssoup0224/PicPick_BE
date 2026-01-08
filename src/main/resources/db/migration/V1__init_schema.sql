-- Create marts table
CREATE TABLE marts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    address VARCHAR(255),
    registration_number DECIMAL(20, 0) UNIQUE NOT NULL,
    document_file VARCHAR(255),
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION
);

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME(6),
    last_login DATETIME(6),
    total_scans INTEGER,
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    mart_id BIGINT,
    FOREIGN KEY (mart_id) REFERENCES marts (id)
);

-- Create mart_items table
CREATE TABLE mart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price INTEGER,
    start_date DATE,
    end_date DATE,
    mart_id BIGINT,
    FOREIGN KEY (mart_id) REFERENCES marts (id)
);

-- Create scans table
CREATE TABLE scans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_name VARCHAR(255) NOT NULL,
    scan_price INTEGER NOT NULL,
    scanned_at DATETIME(6),
    naver_product_id VARCHAR(255),
    naver_brand VARCHAR(255),
    naver_maker VARCHAR(255),
    naver_name VARCHAR(255),
    naver_price INTEGER,
    naver_image VARCHAR(255),
    ai_unit_price VARCHAR(255),
    user_id BIGINT,
    mart_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (mart_id) REFERENCES marts (id)
);

-- Create gemini table
CREATE TABLE gemini (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    naver_image VARCHAR(255),
    naver_brand VARCHAR(255),
    scan_name VARCHAR(255),
    category VARCHAR(255),
    pick_score DOUBLE PRECISION,
    reliability_score DOUBLE PRECISION,
    scan_price DOUBLE PRECISION,
    naver_price DOUBLE PRECISION,
    price_diff DOUBLE PRECISION,
    is_cheaper BOOLEAN,
    ai_unit_price VARCHAR(255),
    quality_summary TEXT,
    price_summary TEXT,
    conclusion TEXT,
    scan_id BIGINT UNIQUE,
    user_id BIGINT,
    FOREIGN KEY (scan_id) REFERENCES scans (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Create gemini_indicators table
CREATE TABLE gemini_indicators (
    gemini_id BIGINT NOT NULL,
    name VARCHAR(255),
    reason TEXT,
    FOREIGN KEY (gemini_id) REFERENCES gemini (id)
);

-- Create login_logs table
CREATE TABLE login_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    login_time DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
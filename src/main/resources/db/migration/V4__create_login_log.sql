CREATE TABLE login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_at DATETIME(6) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    CONSTRAINT fk_login_log_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);
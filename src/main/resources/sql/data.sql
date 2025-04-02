CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      pw VARCHAR(255) NOT NULL,
                      name VARCHAR(50) NOT NULL,
                      address VARCHAR(255),
                      age INT,
                      email VARCHAR(100) NOT NULL UNIQUE,
                      phonenumber VARCHAR(20),
                      -- user_type ENUM('individual', 'company') NOT NULL,  -- 개인 또는 기업 구분
                      user_type VARCHAR(20) NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_verification (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    code VARCHAR(255),
                                    created_at DATETIME(6),
                                    email VARCHAR(255),
                                    verified BIT(1)
);

TRUNCATE TABLE jobbridge.user;
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

TRUNCATE TABLE jobbridge.user;
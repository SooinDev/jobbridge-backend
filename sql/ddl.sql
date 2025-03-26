CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      pw VARCHAR(255) NOT NULL,
                      name VARCHAR(50) NOT NULL,
                      address VARCHAR(255),
                      age INT,
                      email VARCHAR(100) NOT NULL UNIQUE,
                      phonenumber VARCHAR(20),
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
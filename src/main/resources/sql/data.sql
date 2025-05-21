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

CREATE TABLE resume (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        content TEXT,
                        user_id BIGINT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 인덱스 추가
CREATE INDEX idx_resume_user_id ON resume(user_id);
CREATE INDEX idx_resume_created_at ON resume(created_at);

CREATE TABLE job_posting (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                title VARCHAR(255) NOT NULL,
                                description TEXT,
                                position VARCHAR(100) NOT NULL,
                                required_skills VARCHAR(255),
                                experience_level VARCHAR(100),
                                location VARCHAR(255),
                                salary VARCHAR(100),
                                deadline TIMESTAMP,
                                company_id BIGINT, -- 사람인 데이터는 NULL 허용해야 하므로 NOT NULL 제거
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                source VARCHAR(20) NOT NULL DEFAULT 'USER', -- ✅ USER or SARAMIN
                                url VARCHAR(255), -- ✅ 사람인 공고 URL
                                FOREIGN KEY (company_id) REFERENCES user(id)
);

-- 인덱스 추가
CREATE INDEX idx_job_posting_company_id ON job_posting(company_id);
CREATE INDEX idx_job_posting_created_at ON job_posting(created_at);
CREATE INDEX idx_job_posting_deadline ON job_posting(deadline);

CREATE TABLE job_application (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 job_posting_id BIGINT NOT NULL,
                                 resume_id BIGINT NOT NULL,
                                 applicant_id BIGINT NOT NULL,
                                 status VARCHAR(50) DEFAULT 'PENDING',
                                 application_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 FOREIGN KEY (job_posting_id) REFERENCES job_posting(id),
                                 FOREIGN KEY (resume_id) REFERENCES resume(id),
                                 FOREIGN KEY (applicant_id) REFERENCES user(id)
);

-- 인덱스 추가
CREATE INDEX idx_application_job_posting_id ON job_application(job_posting_id);
CREATE INDEX idx_application_applicant_id ON job_application(applicant_id);
CREATE INDEX idx_application_status ON job_application(status);

CREATE TABLE job_bookmark (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              job_posting_id BIGINT NOT NULL,
                              user_id BIGINT NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (job_posting_id) REFERENCES job_posting(id),
                              FOREIGN KEY (user_id) REFERENCES user(id),
                              UNIQUE KEY unique_user_job (user_id, job_posting_id)
);

-- 인덱스 추가
CREATE INDEX idx_bookmark_user_id ON job_bookmark(user_id);

-- data.sql에 추가
CREATE TABLE IF NOT EXISTS password_reset_tokens (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            token VARCHAR(255) NOT NULL UNIQUE,
                            email VARCHAR(255) NOT NULL,
                            expiry_date TIMESTAMP NOT NULL
);

TRUNCATE TABLE jobbridge.user;
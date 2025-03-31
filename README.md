# JobBridge Backend - AI 기반 구인구직 플랫폼 API 서버 🖥️

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.1.0-6DB33F?logo=spring-boot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="License" />
</p>

<p align="center">
  <i>JobBridge의 핵심 기능을 제공하는 강력하고 확장 가능한 백엔드 서버</i>
</p>

## 📋 프로젝트 소개

JobBridge 백엔드는 Spring Boot 기반의 RESTful API 서버로, AI 기반 구인구직 매칭 서비스의 핵심 기능을 제공합니다. 사용자 인증, 이력서 관리, 채용공고 관리, 지원 프로세스 등 핵심 비즈니스 로직을 처리합니다.

## ✨ 주요 기능

- **사용자 관리**: 회원가입, 로그인, 프로필 관리
- **이력서 관리**: 이력서 작성, 수정, 삭제, 조회
- **채용공고**: 채용공고 등록, 검색, 필터링
- **AI 매칭**: 사용자 프로필과 채용공고 간 최적 매칭 알고리즘
- **지원 프로세스**: 지원서 제출, 상태 조회, 알림 서비스

## 🛠️ 기술 스택

- **Framework**: Spring Boot 3.1.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA & Hibernate
- **Security**: Spring Security & JWT
- **API Documentation**: Swagger/OpenAPI
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Gradle
- **CI/CD**: GitHub Actions

## 🚀 시작하기

### 필수 조건

- Java 17 이상
- MySQL 8.0
- Gradle 7.x 이상

### 설치 방법

```bash
# 저장소 클론
git clone https://github.com/your-username/jobbridge-backend.git

# 디렉토리 이동
cd jobbridge-backend

# 그래들 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

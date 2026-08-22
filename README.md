# Travel Diary Backend

사진의 시간·위치 정보를 기반으로 여행 기록을 타임라인과 지도 형태로 구성하고,
동행자를 초대해 함께 기록을 공유할 수 있는 **Travel Diary 웹 서비스의 Backend**입니다.

회원 인증, 여행 관리, 사진 관리, 참여자 초대, 알림 등의 기능을 REST API로 제공합니다.

## Repository

* **Frontend:** https://github.com/ShinWonJin01/travel-diary-web
* **Backend:** https://github.com/ShinWonJin01/travel-diary-backend

## Tech Stack

### Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* JWT

### Database

* MySQL

### Build / Tool

* Maven
* Git / GitHub

### External API

* Kakao Local API
* 지도 및 위치 정보 관련 외부 API

## Main Features

### Authentication

* 회원가입
* 로그인
* JWT 발급 및 인증
* 인증된 사용자 정보 조회
* Spring Security 기반 API 접근 제어
* BCrypt 기반 비밀번호 암호화

### User

* 회원 정보 조회
* 프로필 정보 수정
* 프로필 이미지 관리

### Trip

* 여행 생성
* 여행 목록 조회
* 여행 상세 조회
* 여행 정보 수정 및 삭제
* 여행 참여자 관리
* 여행별 사용자 권한 검증

### Invitation

* 여행 참여자 초대
* 받은 초대 조회
* 보낸 초대 조회
* 초대 수락
* 초대 거절
* 초대 취소

### Photo

* 여행 사진 업로드
* 여행별 사진 조회
* 사진 정보 관리
* 사진 위치 정보 저장 및 수정
* 이미지 파일 검증
* 사용자 권한에 따른 사진 수정·삭제 제어

### Location

* 사진의 위도·경도 정보 저장
* 사진 위치 변경 API 제공
* 장소명·주소 기반 위치 검색 연동
* 여행 지도에 필요한 위치 데이터 제공

### Notification

* 사용자 알림 조회
* 읽지 않은 알림 개수 조회
* 알림 읽음 처리

### Home

* 최근 여행 정보 제공
* 최근 활동 내역 조회
* 일정 시간 내 발생한 활동 그룹화

## Backend Architecture

Backend는 Controller, Service, Repository 계층을 분리하여 구성했습니다.

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
MySQL
```

각 계층의 역할을 분리하여 요청 처리, 비즈니스 로직, 데이터 접근이 하나의 코드에 집중되지 않도록 구성했습니다.

## Project Structure

```text
src/main/java
├── config          # Spring 및 보안 관련 설정
├── controller      # REST API 요청 처리
├── dto             # 요청·응답 데이터 전달 객체
├── domain          # Entity
├── repository      # JPA Repository
├── service         # 비즈니스 로직
├── security        # JWT 및 인증·인가 처리
└── ...
```

> 패키지는 기능별 역할에 따라 분리하여 Controller에서 직접 데이터 접근 로직을 처리하지 않도록 구성했습니다.

## Authentication Flow

로그인 성공 시 Backend에서 JWT를 발급하고,
인증이 필요한 API 요청에서는 전달받은 토큰을 검증하여 사용자 정보를 확인합니다.

```text
Login Request
      ↓
User Authentication
      ↓
JWT Issue
      ↓
Client
      ↓
Authorization Header
      ↓
Spring Security
      ↓
JWT Validation
      ↓
Protected API
```

인증이 필요한 API는 Spring Security를 통해 보호하고,
Controller에서는 인증된 사용자 정보를 기준으로 요청을 처리하도록 구성했습니다.

## Authorization

단순히 로그인 여부만 확인하는 것이 아니라, 여행과 사진에 대한 사용자의 관계를 확인해 기능 접근 권한을 검증합니다.

예를 들어 사진 위치 수정과 같은 기능에서는 다음과 같은 정보를 확인합니다.

* 여행 소유자 여부
* 사진 업로드 사용자 여부
* 여행에 참여하고 있는 사용자 여부

이를 통해 인증된 사용자라도 권한이 없는 다른 여행의 데이터를 임의로 수정하지 못하도록 처리했습니다.

## File Upload Security

사진 업로드 기능에서는 파일을 그대로 저장하지 않고 업로드 과정에서 검증을 수행하도록 구성했습니다.

* JPEG, PNG, WebP 이미지 파일 허용
* 파일 크기 제한
* UUID 기반 파일명 생성
* 여행 사진과 프로필 이미지 저장 경로 분리
* 저장 경로 검증
* 파일 삭제 시 허용된 경로인지 확인

이를 통해 업로드 파일명 충돌과 비정상적인 파일 접근 가능성을 줄이도록 구현했습니다.

## How to Run

### 1. Repository Clone

```bash
git clone https://github.com/ShinWonJin01/travel-diary-backend.git
cd travel-diary-backend
```

### 2. Requirements

실행 전 다음 환경이 필요합니다.

* Java
* MySQL

### 3. Environment Configuration

프로젝트 실행에 필요한 정보는 소스 코드에 직접 작성하지 않고 환경변수로 관리합니다.

실행 환경에 맞게 다음 항목을 설정해야 합니다.

* MySQL 접속 정보
* JWT Secret
* 외부 API Key

> 실제 Secret Key와 데이터베이스 비밀번호는 GitHub Repository에 업로드하지 않습니다.

### 4. Run Backend

Windows PowerShell 기준:

```bash
.\mvnw.cmd spring-boot:run
```

빌드만 수행하려면 다음 명령어를 사용할 수 있습니다.

```bash
.\mvnw.cmd clean package
```

## Frontend

Frontend는 Vue 3와 TypeScript 기반의 별도 프로젝트로 구성되어 있습니다.

**Frontend Repository**
https://github.com/ShinWonJin01/travel-diary-web

Frontend에서 REST API를 호출하여 회원, 여행, 사진, 초대, 알림 등의 기능을 사용합니다.

## My Role

개인 프로젝트로 Frontend부터 Backend까지 전체 웹 서비스 개발을 진행했습니다.

Backend에서는 다음 기능을 구현했습니다.

* Spring Boot 기반 REST API 설계 및 구현
* Spring Data JPA와 MySQL 기반 데이터 저장·조회·수정 처리
* Spring Security 및 JWT 기반 인증·인가 구현
* BCrypt를 활용한 비밀번호 암호화
* 여행 생성·조회·수정·삭제 기능 구현
* 여행 참여자 및 초대 관리 기능 구현
* 사진 업로드 및 위치 정보 관리 기능 구현
* 여행과 사용자 관계를 기준으로 한 API 권한 검증
* 알림 조회 및 읽음 처리 기능 구현
* 홈 화면 최근 활동 API 구현
* 외부 위치 검색 API 연동
* 이미지 업로드 파일 형식·크기·경로 검증
* DB, JWT, 외부 API Key 환경변수 분리

## Troubleshooting

### 인증된 사용자의 데이터 접근 권한 처리

#### Problem

JWT 인증만 적용할 경우 로그인한 사용자라는 사실은 확인할 수 있지만, 해당 사용자가 요청한 여행이나 사진을 실제로 수정할 권한이 있는지는 별도로 판단해야 했습니다.

인증된 사용자가 URL의 여행 ID나 사진 ID를 변경해 다른 사용자의 데이터에 접근할 가능성을 막을 필요가 있었습니다.

#### Cause

인증(Authentication)과 권한 확인(Authorization)은 서로 다른 과정이기 때문에, JWT 검증만으로는 특정 여행이나 사진에 대한 접근 권한을 판단할 수 없었습니다.

#### Solution

Service 계층에서 요청한 사용자와 여행·사진 데이터의 관계를 확인하도록 구현했습니다.

여행 소유자, 사진 업로드 사용자, 여행 참여 여부 등을 확인한 뒤 허용된 사용자만 수정 기능을 수행할 수 있도록 권한 검증 로직을 추가했습니다.

#### Result

단순히 로그인한 사용자라는 이유만으로 모든 여행 데이터에 접근할 수 없도록 제한하고, 사용자와 데이터의 관계를 기준으로 API 접근 권한을 제어할 수 있도록 개선했습니다.

## Security

배포를 고려해 다음과 같은 기본 보안 조치를 적용했습니다.

* Spring Security 기반 인증 API 보호
* JWT 기반 Stateless 인증
* BCrypt 비밀번호 암호화
* DB 접속 정보 환경변수 관리
* JWT Secret 환경변수 관리
* 외부 API Key 환경변수 관리
* 업로드 가능 이미지 형식 제한
* 업로드 파일 크기 제한
* UUID 기반 파일명 저장
* 파일 저장·삭제 경로 검증
* CORS 허용 Origin 제한
* 서버 오류 응답에서 내부 Stack Trace 노출 제한

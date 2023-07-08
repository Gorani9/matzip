# Matzip: 맛집 리뷰 SNS

## 1. 프로젝트 소개

### 1.0. 배포

- 배포 중단

### 1.1. 프로젝트 목적

- 간단한 맛집 리뷰 SNS를 만들어보자

### 1.2. 기술 스택

#### FrontEnd

<img src="https://img.shields.io/badge/Svelte%20Kit%201%2E5%2E0-FF3E00? style=for-the-badge&logo=Svelte&logoColor=white">

#### BackEnd

<img src="https://img.shields.io/badge/Spring%20Boot%202%2E7%2E2-6DB33F? style=for-the-badge&logo=Spring%20Boot&logoColor=white">

<img src="https://img.shields.io/badge/Hibernate%205%2E6%2E10%20Final-59666C? style=for-the-badge&logo=Hibernate&logoColor=white">

#### Database

<img src="https://img.shields.io/badge/MySQL%208%2E0-4479A1?%style=for-the-badge&logo=MySQL&logoColor=white">

<img src="https://img.shields.io/badge/Redis%207%2E0-DC382D?%style=for-the-badge&logo=Redis&logoColor=white">

#### Infra / Deployment

<img src="https://img.shields.io/badge/EC2-FF9900?%style=for-the-badge&logo=Amazon%20EC2&logoColor=white">

<img src="https://img.shields.io/badge/Docker%E2%80%93Compose-2496ED?%style=for-the-badge&logo=Docker&logoColor=white">

<img src="https://img.shields.io/badge/Vercel-000000?%style=for-the-badge&logo=Vercel&logoColor=white">

<img src="https://img.shields.io/badge/S3-569A31?%style=for-the-badge&logo=Amazon%20S3&logoColor=white">

### 1.3. 프로젝트 기능

- 리뷰 작성 및 수정
- 댓글 작성 및 수정
- 리뷰 상호작용: 좋아요, 스크랩
- 유저 상호작용: 팔로우, 팔로잉
- 리뷰, 유저 검색

### 1.4. Future Works

- 피드 개선
- 리뷰, 유저 검색 기능 개선
- 위치기반 맛집 추천 기능
- 유저 랭킹, 인기 맛집 기능
- 알림 기능

## 2. 프로젝트 구조

### 2.1. 프로젝트 구조

```bash
.
├── README.md
├── app
└── server
```

`app` 디렉토리에는 `SvelteKit 1.5.0`을 사용하여 개발된 애플리케이션의 프론트엔드가 포함되어 있습니다.

`server` 디렉토리에는 `Spring Boot 2.7.2`를 사용하여 개발된 애플리케이션의 백엔드가 포함되어 있습니다. 

### 2.2. 인프라 구조

![Infra](https://user-images.githubusercontent.com/41163414/227232489-f7f462a3-0a8c-4545-911c-3e3c2910a727.png)

## 3. Getting Started

### 3.0. Prerequisites

- Docker Compose
- Node.js, npm
- AWS S3

### 3.1. FrontEnd

로컬에서 서버와 통신하기 위해서 설정을 바꿔주어야 합니다.

`app/src/lib/api.ts`에서 `DOMAIN`을 다음과 같이 변경합니다.

```ts
export const DOMAIN = 'http://localhost:8080';
```

그리고 `app` 디렉토리에서 다음 명령어를 실행합니다.

```bash
npm install
npm run dev
```

### 3.2. BackEnd

서버를 로컬에서 돌리기 위해서 몇 가지 추가적인 설정이 필요합니다.

먼저 AWS S3에 버킷을 생성합니다. 버킷 이름은 `matzip-s3`로 하겠습니다.

그리고 `server/src/main/resources/application.yml`에 다음과 같이 변경합니다.

```yaml
cloud:
  aws:
    credentials:
        accessKey: ${AWS_ACCESS_KEY}
        secretKey: ${AWS_SECRET_KEY}
    s3:
        bucket: matzip-s3
        region: ap-northeast-2

matzip:
  jwt:
    secret: ${JWT_SECRET}
```

`docker-compose.yml`에 `caddy` 설정을 주석처리하거나 삭제합니다.
그리고 `server`설정을 다음과 같이 설정합니다.

```yaml
  server:
    container_name: spring
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    extra_hosts:
      - "host.docker.internal:host-gateway"
    environment:
      SPRING_PROFILES_ACTIVE: local # Change "dev" to "local"
      SPRING_DATASOURCE_URL: jdbc:mysql://host.docker.internal:3306/matzip_db
      SPRING_DATASOURCE_USERNAME: matzip_admin
      SPRING_DATASOURCE_PASSWORD: MATZIP_admin_01
      SPRING_REDIS_HOST: host.docker.internal
      SPRING_REDIS_PORT: 6379
```

그리고 DB 파일을 저장할 경로도 필요하다면 바꿔줍니다.
기본값은 `./data` 현재 디렉토리입니다.

```yaml
    rdb:
    container_name: mysql
    image: mysql:8.0
    ports:
      - "3306:3306"
    volumes:
      - ./data:/var/lib/mysql:rw # Change Here
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
    environment:
      MYSQL_DATABASE: matzip_db
      MYSQL_USER: matzip_admin
      MYSQL_PASSWORD: MATZIP_admin_01
      MYSQL_ROOT_PASSWORD: root
```

마지막으로 `server` 디렉토리에서 다음 명령어를 실행합니다.

```bash
docker-compose up -d
```

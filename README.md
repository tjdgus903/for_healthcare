🧠 NeuroCare Play

인지 기능 향상 및 건강 관리용 게임 앱
시니어(치매·파킨슨 환자) 대상 맞춤형 브레인 트레이닝 프로그램
보호자, 기관, 의료진을 위한 관리·모니터링 기능 제공

📌 프로젝트 개요

프로젝트명: NeuroCare Play

목표: 고령자 및 보호자가 함께 참여할 수 있는 게임 기반 인지 건강 플랫폼 구축

개발 기간: 2025.08 ~ (진행 중)

주요 기능:

🧩 게임 플레이 (Color Tap / Sequence Memory / Shape Match)

📅 출석 체크 및 리워드

🎁 광고 보상 및 구독 기반 수익화

👨‍👩‍👧 보호자-플레이어 연결 모델

🏢 기관 콘솔 (기관 단위 사용자 관리, 통계 리포트)

🔒 JWT 인증 기반 로그인

📈 리포트 및 데이터 시각화

⚙️ 관리자/운영자 콘솔

🏗️ 기술 스택
구분	기술
Backend	Spring Boot 3 (Kotlin) + JPA + JWT + Flyway
Frontend	Vite + Vanilla JS + Capacitor (Hybrid App)
Database	PostgreSQL 17 (prod), H2 (local test)
Monitoring (예정)	Micrometer + Prometheus + Grafana
CI/CD (예정)	GitHub Actions + Docker
Infra	Rocky Linux 9.6 (VM, 192.168.56.103)
Language	Kotlin, HTML/CSS/JS
Etc.	Swagger (API Docs), Caffeine Cache, AdMob SDK (rewarded ads)
🧩 주요 기능 흐름
M1~M9 단계별 구현 내역
단계	구현 내용	상태
M1. 기본 환경 & 인프라	Kotlin + Spring Boot + PostgreSQL 기반 환경 구성	✅ 완료
M2. 인증/권한	JWT 로그인/로그아웃, Swagger 보안 설정	✅ 완료
M3. 보호자-플레이어 연결	초대 코드 기반 연결, 1:N 관계 매핑	✅ 완료
M4. 출석체크 고도화	출석 달력, 보상 포인트, 리포트 반영	✅ 완료
M5. 게임 코어 (3종)	Color Tap / Sequence Memory / Shape Match API 및 세션/메트릭 로직 구현	✅ 완료
M6. 광고 & 수익화	보상형 광고 API, AdMob SDK 연동 준비	✅ 완료
M7. 구독 모델	Subscription API, 상태 캐싱(Caffeine), 만료 로직 구현	✅ 완료
M8. 기관 콘솔 (B2B)	기관/코호트 관리, 종합 리포트, 사용자 통계 API	✅ 완료
M9. 개인정보/보안	계정 보호, 토큰 만료, 암호화 Key 설정	✅ 완료
M10. 운영 & 배포	모니터링 / CI-CD / 로그 프로세스 구축	🚧 진행 예정
⚙️ 실행 방법
1️⃣ 백엔드 실행
# 환경 변수 설정 (Windows PowerShell 기준)
$env:JWT_SECRET = "dev-please-change"
$env:CRYPTO_AESKEYBASE64 = "bXktMzItYnl0ZS1yYW5kb20ta2V5"

# 실행
./gradlew bootRun

2️⃣ PostgreSQL 설정 (VM: 192.168.56.103)
CREATE USER fhc WITH PASSWORD 'fhc';
CREATE DATABASE healthcare OWNER fhc;
GRANT ALL PRIVILEGES ON DATABASE healthcare TO fhc;


application-prod.yaml

spring:
  datasource:
    url: jdbc:postgresql://192.168.56.103:5432/healthcare
    username: fhc
    password: fhc
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration

3️⃣ 프론트엔드 실행
cd app
npm install
npm run dev


/app/.env

VITE_API_BASE=http://localhost:8080
VITE_ADMOB_REWARDED_ID=ca-app-pub-3940256099942544/5224354917


브라우저 실행: http://localhost:5173

🧠 주요 페이지 구성
구분	파일 경로	기능
메인	/index.html	홈 화면
로그인	/login.html	JWT 로그인 (localStorage 저장)
게임	/games/index.html	Color Tap / Sequence Memory / Shape Match
출석	/attendance/index.html	달력 기반 출석 리워드
리포트	/reports/index.html	게임/출석/구독 통계
구독	/subs/index.html	구독 상태 및 보상 광고 비활성화
공통 컴포넌트	/dev/header.html, /dev/footer.html, /css/app.css	상단/하단 UI, 고대비 모드
🔒 인증 구조 (JWT)

/auth/login → JWT Token 발급

localStorage.jwt에 저장

fetch() 요청 시 자동으로 Authorization: Bearer <token> 헤더 추가

로그인 여부에 따라 헤더 버튼 동적 표시 (로그인/로그아웃 토글)

🎮 게임 구조

세션 시작: /sessions/start

Body: { "gameType": "COLOR_TAP" }

서버가 GameSession 생성 후 sessionId 반환

세션 종료: /sessions/end

Body: { "sessionId": "...", "score": ..., "accuracy": ... }

메트릭 저장: SessionMetric API 자동 연동

💰 수익화 구조

보상형 광고: AdMob SDK (Capacitor + Community Plugin)

구독 모델:

Android / iOS 검증 API Stub 완료

SubscriptionService.verify() 기반 상태 갱신

구독 유저는 광고 OFF 처리

🏢 기관 콘솔 (Org Console)

기관/코호트별 통계 리포트

기관 → 코호트 → 사용자 → 세션 구조

/orgs/report API 통해 종합 리포트 제공

📊 운영 계획 (M10 이후)
항목	기술	비고
모니터링	Prometheus + Grafana	Spring Actuator + Micrometer
CI/CD	GitHub Actions	Docker build → test → deploy
로그 관리	Logback + ELK(옵션)	Application + System 로그 통합
백업/복구	pgBackRest / pg_rman	운영 환경에서 주기적 백업 자동화
배포 환경	Docker 또는 VM (Rocky 9.6)	포트 8080 (API), 5173 (App)
👥 기여 및 개발 원칙

Kotlin + Spring Boot 3.x + Gradle 기반

Entity / DTO / Controller / Service / Repository 구조 명확화

API 변경 시 Swagger 문서 자동 반영

하드코딩 금지, 환경별 YAML 분리 (dev, prod)

배포 전 Flyway로 schema 관리 필수

🧾 TODO / 향후 개선

 Prometheus + Grafana 통합 (서버 상태 모니터링)

 GitHub Actions CI/CD 파이프라인 구축

 AdMob SDK 실제 연동 (Android/iOS)

 UI/UX 개선 (고대비, 큰 폰트, 음성 안내)

 보호자 앱 / 관리자 웹 분리 배포

 통합 리포트 대시보드 완성

📅 현재 단계:
✅ M9 개인정보/보안까지 완료
🚧 M10 운영·모니터링 단계 진행 준비 중

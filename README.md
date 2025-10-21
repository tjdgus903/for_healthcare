# 🧠 NeuroCare Play
인지 기능 향상 및 건강 관리용 게임 앱  
치매·파킨슨 환자 등 고령층 대상 맞춤형 브레인 트레이닝 프로그램  
보호자, 기관, 의료진을 위한 관리 및 모니터링 기능 제공

---

## 📌 프로젝트 개요
- **프로젝트명:** NeuroCare Play  
- **목표:** 시니어 대상 인지 건강 향상 및 보호자·기관 관리 기능 제공  
- **개발 기간:** 2025.08 ~ (진행 중)  

## 🧩 주요 기능
### 🎮 게임 코어
- Color Tap / Sequence Memory / Shape Match (3종)
- 세션·점수·정확도 기록 및 저장
- 사용자별 게임 메트릭 집계 (리포트 기능 연동)

### 📅 출석체크
- 일자별 출석 달력 표시  
- 연속 출석 리워드 시스템  
- 출석 기반 리포트 생성

### 👨‍👩‍👧 보호자-플레이어 연결
- 초대 코드 기반 1:N 연결 구조  
- 보호자 계정을 통한 활동 모니터링

### 💰 광고 & 수익화
- **보상형 광고 (Rewarded Ad)**만 도입 — 세션 중 강제 광고 없음  
- 구독 회원은 광고 완전 제거  
- AdMob SDK + Capacitor 연동

### 💎 구독 모델
- Android/iOS 결제 검증 Stub 구현  
- Subscription 상태 관리 및 캐싱  
- 구독자 혜택: 광고 제거, 리포트 확장, 보호자 무제한 연결

### 🏢 기관 콘솔
- 기관별 / 코호트별 사용자 관리  
- 기관 리포트 (통계, PDF/CSV 내보내기)  
- 게임/출석 통합 분석

### 🔒 보안 & 운영
- JWT 인증 기반 로그인  
- Caffeine 캐시 / Token 만료 처리  
- Flyway 마이그레이션 기반 DB 버전 관리  
- Docker / VM 배포 대응

---

## 📊 데이터 리포트 기능
- **API:** `/report/me`  
- **집계 내용:**  
  - 게임별 평균 점수, 정확도, 총 플레이 시간  
  - 최근 30일간 세션 수 / 리워드 통계  
- **프론트:** `/reports/index.html`  
  - “내 리포트 불러오기” 버튼으로 실시간 호출  
  - JSON → 시각화 확장 준비 완료 (그래프/차트 연동 가능)

---

## ⚙️ 기술 스택
| 구분 | 사용 기술 |
|------|------------|
| **Backend** | Kotlin + Spring Boot 3.x + JPA + Flyway + JWT |
| **Frontend** | Vite + Vanilla JS + Capacitor (Hybrid App) |
| **Database** | PostgreSQL 17 (Prod), H2 (Dev) |
| **Infra** | Rocky Linux 9.6 (VM 기반, 192.168.56.103) |
| **Build/CI** | Gradle + GitHub Actions |
| **Others** | Swagger, Bootstrap 5 |


---

## 🧩 단계별 개발 현황
| 단계 | 구현 내용 | 상태 |
|------|------------|------|
| M1 | 기본 환경 & 인프라 구성 | ✅ 완료 |
| M2 | JWT 인증/권한 | ✅ 완료 |
| M3 | 보호자–플레이어 연결 | ✅ 완료 |
| M4 | 출석체크 고도화 | ✅ 완료 |
| M5 | 게임 코어 (3종) | ✅ 완료 |
| M6 | 광고 & 수익화 | ✅ 완료 |
| M7 | 구독 모델 | ✅ 완료 |
| M8 | 기관 콘솔 (B2B) | ✅ 완료 |
| M9 | 개인정보/보안 강화 | ✅ 완료 |
| M10 | 운영 & 배포 (모니터링, CI/CD) | 🚧 진행 예정 |

---

## ⚙️ 실행 방법

### 1️⃣ 백엔드 실행
```bash
# (Windows PowerShell 기준)
$env:JWT_SECRET = "dev-please-change"
$env:CRYPTO_AESKEYBASE64 = "bXktMzItYnl0ZS1yYW5kb20ta2V5"

./gradlew bootRun

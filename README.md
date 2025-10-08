# 🧠 NeuroCare Play
인지 기능 향상 및 건강 관리용 게임 앱  
치매·파킨슨 환자 등 고령층 대상 맞춤형 브레인 트레이닝 프로그램  
보호자, 기관, 의료진을 위한 관리 및 모니터링 기능 제공

---

## 📌 프로젝트 개요
- **프로젝트명:** NeuroCare Play  
- **목표:** 시니어 대상 인지 건강 향상 및 보호자·기관 관리 기능 제공  
- **개발 기간:** 2025.08 ~ (진행 중)  
- **주요 기능:**  
  - 🧩 게임 플레이 (Color Tap / Sequence Memory / Shape Match)  
  - 📅 출석 체크 및 리워드  
  - 🎁 광고 보상 및 구독 기반 수익화  
  - 👨‍👩‍👧 보호자–플레이어 연결  
  - 🏢 기관 콘솔 (B2B 관리, 리포트)  
  - 🔒 JWT 인증 로그인  
  - 📈 리포트 및 데이터 시각화  
  - ⚙️ 관리자/운영자 콘솔  

---

## 🏗️ 기술 스택
| 구분 | 기술 |
|------|------|
| **Backend** | Spring Boot 3 (Kotlin) + JPA + JWT + Flyway |
| **Frontend** | Vite + Vanilla JS + Capacitor (Hybrid App) |
| **Database** | PostgreSQL 17 (prod), H2 (local test) |
| **Monitoring** | Micrometer + Prometheus + Grafana *(예정)* |
| **CI/CD** | GitHub Actions + Docker *(예정)* |
| **Infra** | Rocky Linux 9.6 (VM, 192.168.56.103) |
| **Etc.** | Swagger, Caffeine Cache, AdMob SDK |

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

# for_healthcare

 # 📌 전체 개발 로드맵 (업데이트 반영 버전)

## M1. 기본 환경 & 인프라 (✔ 진행됨)

- ✔ **프로젝트 부트스트랩** (Spring Boot 3 + Kotlin + JPA + H2/PG 분리)
- ✔ **출석체크 기능** (`/attendance/check`, `/attendance/calendar`)
- ✔ **DevDataLoader**로 기본 유저 생성
- ⬜ **Swagger UI 추가** → 브라우저에서 테스트 가능하게

---

## M2. 인증/권한 (✔ 진행됨)

- ⬜ **JWT 인증** 도입
    - 로그인 API (`POST /auth/login {id,pw}`) → JWT 발급
    - `X-User-Id` 제거, JWT 주체 기반 권한 관리
    - Spring Security 설정 (권한별 접근 제한)
- ⬜ **Swagger UI Authorization 헤더 연동**

---

## M3. 보호자–플레이어 연결 (✔ 진행됨)

- ⬜ 초대 코드 발급 (`POST /care/invite`)
- ⬜ 코드 수락 (`POST /care/accept`) → 관계 ACTIVE
- ⬜ 연결 조회 (`GET /care/relations`)
- 정책: 초대 코드 만료/1회용, 권한 검증 필수

---

## M4. 출석체크 고도화 (✔ 진행됨)

- ⬜ 연속 출석 리워드 (배지, 힌트)
- ⬜ 출석 리포트 (`/reports/me`, `/reports/player/{id}`)
- ⬜ 알림/리마인드(푸시 or 단순 로컬 알림)

---

## M5. 게임 코어 (MVP 3종) (✔ 진행됨)

- ⬜ 게임 3종 구현 (Color Tap, Sequence Memory, Shape Match)
- ⬜ `Game` / `Session` / `SessionMetric` 엔티티
    - 공통 지표 + **game_meta(JSONB)**로 확장성 확보
- ⬜ 게임 API (`/games`, `/sessions`)
- ⬜ 접근성(큰 버튼/고대비/떨림 보정)

---

## M6. 광고 & 수익화 (부담 최소화 정책 반영) (✔ 진행됨)

- **광고 정책 (부담 줄이기):**
    - ❌ 세션 중간 강제 광고 없음
    - ✅ **보상형 광고만** (힌트, 시간 추가, 아이템 unlock 시)
    - ✅ 세션 종료 후 1회 광고 노출 제한
    - ✅ 구독 사용자 → 광고 완전 제거
- ⬜ AdMob/구글 보상형 광고 SDK 연동 (안드로이드)
- ⬜ 백엔드 플래그 (`/ads/config`)로 광고 노출 정책 제어

---

## M7. 구독 모델 (✔ 진행됨)

- ⬜ 구독 상품: 광고 제거, 리포트 기간 확장, 보호자 연결 무제한
- ⬜ 결제: Google Play Billing + 서버 영수증 검증
- ⬜ 구독 상태 캐싱 및 권한 미들웨어

---

## M8. 기관 콘솔 (B2B 확장) (✔ 진행됨)

- ⬜ 기관 계정, 코호트 관리
- ⬜ 그룹 리포트 (PDF/CSV 내보내기)
- ⬜ 데이터 공유 동의/해지 추적

---

## M9. 개인정보/보안 (✔ 진행됨)

- ⬜ 동의/철회 로그 (Consent)
- ⬜ 최소 수집 원칙 + 민감 데이터 암호화
- ⬜ 데이터 삭제/내보내기 API
- ⬜ 서비스 약관/광고 표기 준수

---

## M10. 운영 & 배포 (⏭ 다음 단계)

- ⬜ 모니터링 (Micrometer + Prometheus + Grafana)
- ⬜ CI/CD (GitHub Actions → Docker → 배포)
- ⬜ Flyway 전환, 마이그레이션 관리
- ⬜ 로그/장애 대응 프로세스
- ⬜ DB 서버 연동(H2/Postgresql)

---



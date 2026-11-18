# EduHub

사내 교육과정 조회·수강 신청 시스템. 정원이 차면 대기자로 등록된다.
요구사항과 결정 사항(D-xx)은 `docs/requirements.md`가 기준이다.

## 디렉터리
- `backend/` Spring Boot 3, Java 21 (규칙: `backend/CLAUDE.md`)
- `frontend/` npm workspaces: `apps/course-mfe`, `packages/api-client` (규칙: `frontend/CLAUDE.md`)
- `docs/` 요구사항
- `.env` 로컬 비밀 정보 (커밋 금지). 변수 이름은 `.env.example`

## 실행
- 백엔드 테스트: `cd backend && ./gradlew test` (Windows: `.\gradlew.bat test`)
- 백엔드 실행: `cd backend && ./gradlew bootRun` (dev 프로필, `.env`의 `DB_*`)
- 프론트 테스트: `cd frontend && npm test -w apps/course-mfe`
- 프론트 실행: `cd frontend && npm run dev -w apps/course-mfe` (포트 3001)

## 공통 규칙
- 브랜치: `종류/요구사항ID-설명` (예: `feature/fr-01-course-list`). `main`에 직접 커밋하지 않는다
- 커밋: Conventional Commits (예: `feat(course): ...`), PR은 `.github/pull_request_template.md` 형식, Squash merge
- 비밀 정보는 코드·설정 파일에 쓰지 않는다. 환경 변수와 `.env`만 사용
- DB는 로컬 PostgreSQL: dev는 `eduhub`, 테스트는 `eduhub_test` (`.env`의 `DB_*`, `TEST_DB_*`). Docker는 쓰지 않는다
- 범위 밖 파일은 고치지 않는다. 애매하면 먼저 묻는다

## 개발 진행 절차 (기능 단위)
1. `/fr-start <요구사항ID>`로 시작한다. 설계 스펙 → 개발자 승인 → 구현 계획 → 개발자 승인 순서로 진행한다
2. 계획의 작업 단위마다 `implementer`로 구현하고 `reviewer`로 검토한다. 심각·경고 항목은 고친다. 수정과 재검토는 2회까지만 하고, 그래도 남으면 개발자에게 보고한다
3. 모든 단위가 끝나면 `critical-reviewer`에게 설계 스펙, 전체 diff, 1차 리뷰 결과를 넘긴다
4. 서브에이전트를 호출할 때 model은 지정하지 않는다. 정의 파일의 `model`을 따른다
5. 3개 이상 파일이나 여러 계층에 걸치는 작업, 1,000줄이 넘는 diff는 시작 전에 개발자에게 먼저 묻는다
6. 수량 변경(정원, 대기 순번)은 반드시 락으로 처리한다. 중복 방지는 DB 유니크 제약으로 강제한다

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

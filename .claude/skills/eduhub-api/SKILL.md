---
name: eduhub-api
description: EduHub 백엔드에 REST API를 추가하거나 바꿀 때 쓰는 팀 절차. 새 엔드포인트 추가, 기존 API의 요청·응답·상태 코드 변경, 새 기능(FR)의 백엔드 구현, 테이블·엔티티·DTO 추가처럼 backend/의 Controller·Service·Repository를 건드리는 작업이면 시작 전에 읽는다. 프론트 화면만 바꾸는 작업에는 쓰지 않는다.
---

# EduHub API 추가 절차

`backend/CLAUDE.md` 규칙을 따른다. 아래 순서대로 진행하고, 해당 없는 단계는 건너뛴다.

## 1. Flyway 마이그레이션
- `backend/src/main/resources/db/migration/V{n}__설명.sql`을 새로 추가한다. 이미 적용된 파일은 고치지 않는다
- 중복 금지 같은 규칙은 DB 제약(유니크·부분 유니크 인덱스, CHECK)으로 강제한다
- 자주 거르는 컬럼 조합에 인덱스를 둔다

## 2. 엔티티 (`{도메인}/domain`)
- setter 없음. 상태 변경은 의도가 드러나는 메서드(`cancel()`, `close()` 등)로
- 연관관계는 `FetchType.LAZY`
- 업무 규칙 위반은 `BusinessException` 하위 예외를 `domain`에 두고 던진다 (HTTP 상태와 코드를 생성자에서 정한다)

## 3. DTO (`{도메인}/api/dto`)
- 요청·응답 모두 record. 엔티티를 응답으로 반환하지 않는다
- 요청에는 Bean Validation(`@NotBlank`, `@Positive` 등), 응답 필드에는 필요하면 `@Schema`

## 4. Repository (`{도메인}/domain`)
- 목록 조회는 N+1이 나지 않게 fetch join이나 집계 쿼리로 (NFR-02)
- 수량(정원, 순번 등)을 바꾸는 흐름은 비관적 락(`@Lock(PESSIMISTIC_WRITE)`) 등으로 직렬화한다 (NFR-01)

## 5. Service (`{도메인}/application`)
- 클래스에 `@Transactional`, 조회 메서드는 `@Transactional(readOnly = true)`
- 락은 트랜잭션 안에서 가장 먼저 잡고, 락을 잡은 뒤 상태를 읽는다
- 날짜·시간 규칙은 주입받은 `Clock`으로 계산한다
- `enrollment`는 `course`를 참조할 수 있지만 반대는 안 된다

## 6. Controller (`{도메인}/api`)
- 경로 `/api/v1/...`, 페이지는 0부터, 목록 응답은 `PageResponse<T>`
- Repository를 직접 부르지 않는다
- `@Tag`, `@Operation(summary)`, 오류마다 `@ApiResponse(content = ErrorResponse)` (NFR-04)
- 예외와 HTTP 상태: 없음 404, 권한 없음 403, 상태·규칙 충돌 409, 입력 오류 400

## 7. 테스트
- Service: Mockito 단위 테스트 (정상, 각 예외, 경계값)
- Controller: `@WebMvcTest` + `@MockitoBean`, 상태 코드와 `$.code`
- Repository: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@ActiveProfiles("test")`
- 동시성 규칙이 있으면 `@SpringBootTest`로 실제 동시 실행 테스트를 쓰고, 락을 빼면 실패하는지 한 번 확인한다

## 8. 프론트 API 클라이언트 재생성
- 백엔드를 띄운 뒤 `cd frontend && npm run generate:api`
- 생성된 `packages/api-client/src/generated`는 손으로 고치지 않는다

## 확인
```bash
cd backend && ./gradlew spotlessApply test
```
모두 통과해야 끝난 것이다.

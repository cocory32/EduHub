# backend 규칙

## 명령
- 테스트: `./gradlew test` (DB 테스트는 `.env`의 `TEST_DB_*` 필요)
- 포맷: `./gradlew spotlessApply` (google-java-format). 커밋 전 `./gradlew spotlessCheck`
- Gradle은 JDK 21로 실행된다 (`~/.gradle/gradle.properties`의 `org.gradle.java.home`)

## 패키지와 계층
- 도메인별 패키지: `com.example.eduhub.{course,enrollment,common}`
- 도메인 안은 `api`(Controller, `dto`) → `application`(Service) → `domain`(엔티티, Repository)
- Controller는 Repository를 직접 부르지 않는다
- `enrollment`는 `course`를 참조할 수 있지만 반대는 안 된다. `common`은 어떤 도메인도 참조하지 않는다

## JPA
- 연관관계는 `FetchType.LAZY`. 목록 조회에서 N+1이 나지 않게 한다 (NFR-02)
- 엔티티에 setter를 만들지 않는다. 상태 변경은 `update()`, `close()` 같은 메서드로
- 엔티티를 API 응답으로 반환하지 않는다. 응답은 `dto`의 record
- `open-in-view: false`, `ddl-auto: validate`. 스키마는 Flyway로만 바꾼다
- 이미 적용된 마이그레이션은 고치지 않는다. 새 버전 `V{n}__설명.sql`을 추가한다

## API
- 경로는 `/api/v1/...`. 페이지는 0부터, 응답은 `PageResponse<T>`
- 오류는 `BusinessException` 하위 클래스로 던지고 `GlobalExceptionHandler`가 `ErrorResponse(code, message)`로 바꾼다
- 모든 API에 `@Tag`, `@Operation(summary)`, 오류 응답 `@ApiResponse`를 단다 (NFR-04)

## 테스트
- Controller: `@WebMvcTest` + `@MockitoBean`. Service: Mockito 단위 테스트
- Repository: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@ActiveProfiles("test")`
- Lombok은 쓰지 않는다

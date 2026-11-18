# EduHub

사내 교육과정 조회, 수강 신청 시스템

## 백엔드 실행

필요한 것: JDK 21(없으면 Gradle이 자동으로 받음), 로컬 PostgreSQL(`eduhub`, `eduhub_test` DB)

1. 저장소 루트에서 `.env.example`을 `.env`로 복사한다
2. 로컬 PostgreSQL 접속 정보를 `.env`에 채운다 (`.env`는 커밋되지 않는다)

```bash
cd backend
./gradlew test            # 테스트 (.env의 TEST_DB_* 사용)
./gradlew spotlessApply   # 코드 포맷 정리
./gradlew bootRun         # dev 프로필로 실행 (.env의 DB_* 사용)
```

Windows PowerShell/cmd에서는 `./gradlew` 대신 `gradlew.bat`을 쓴다.
실행 후 http://localhost:8080/swagger-ui.html 에서 API 문서를 볼 수 있다.

### 패키지 구조 (`com.example.eduhub`)

| 패키지 | 역할 |
|---|---|
| `common` | 여러 도메인이 함께 쓰는 것 (상태 확인 API 등) |
| `course` | 과정, 회차, 정원 |
| `enrollment` | 신청, 대기, 취소, 수료. `course`를 참조할 수 있지만 반대는 안 됨 |

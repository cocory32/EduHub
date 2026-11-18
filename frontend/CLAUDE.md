# frontend 규칙

- UI는 MUI. 서버 호출은 `packages/api-client`의 생성된 클라이언트만 쓴다 (`axios.create` 금지)
- 생성 클라이언트 인스턴스는 앱마다 `src/api/xxxApi.ts` 한 곳에서만 만든다
- 서버 데이터 조회는 커스텀 훅(`src/hooks/useXxx.ts`)으로 감싸고, 화면 컴포넌트는 API를 직접 부르지 않는다
- 페이지 번호는 0부터. 받은 값을 바꾸지 않고 그대로 API에 넘긴다
- 클라이언트 재생성: 백엔드를 띄운 뒤 `npm run generate:api` (JDK 11+ 필요, `JAVA_HOME`을 JDK 21로)

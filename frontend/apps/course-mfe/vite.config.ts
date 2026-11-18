import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 단독 실행용 (Module Federation은 6주차).
// CORS 대신 개발 서버 프록시를 쓴다: 브라우저는 같은 출처(3001)의 /api만 부르고, Vite가 8080으로 넘긴다.
// 그래서 백엔드에 CORS 설정이 필요 없고, 생성 클라이언트의 basePath는 '' 이다.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3001,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});

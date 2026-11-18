import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { Container, CssBaseline, Typography } from '@mui/material';
import { CoursesPage } from './pages/CoursesPage';
import { CourseAdminPage } from './pages/CourseAdminPage';

// 라우터 도입 전 임시 진입: ?view=admin 이면 과정 관리 화면 (관리자 권한 구분은 인증 도입 후)
const isAdmin = new URLSearchParams(window.location.search).get('view') === 'admin';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <CssBaseline />
    <Container sx={{ py: 4 }}>
      <Typography variant="h5" component="h1" gutterBottom>
        {isAdmin ? '교육과정 관리' : '교육과정'}
      </Typography>
      {isAdmin ? <CourseAdminPage /> : <CoursesPage />}
    </Container>
  </StrictMode>,
);

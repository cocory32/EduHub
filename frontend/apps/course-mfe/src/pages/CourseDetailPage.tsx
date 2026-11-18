import {
  Alert,
  Button,
  Chip,
  CircularProgress,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { useCourse } from '../hooks/useCourse';

const STATUS_LABEL: Record<string, string> = { OPEN: '모집 중', CLOSED: '종료' };
const statusLabel = (status?: string) => (status ? (STATUS_LABEL[status] ?? status) : '');

export interface CourseDetailPageProps {
  courseId: number;
  onBack: () => void;
}

/** 과정 상세 화면 (FR-02). 데이터는 useCourse만 쓰고 courseApi를 직접 부르지 않는다. */
export function CourseDetailPage({ courseId, onBack }: CourseDetailPageProps) {
  const { data, loading, error, notFound } = useCourse(courseId);

  const backButton = (
    <Button onClick={onBack} sx={{ alignSelf: 'flex-start' }}>
      ← 목록으로
    </Button>
  );

  if (loading) return <CircularProgress />;

  if (notFound) {
    return (
      <Stack spacing={2}>
        {backButton}
        <Alert severity="info">해당 과정을 찾을 수 없습니다. 삭제되었거나 잘못된 주소일 수 있습니다.</Alert>
      </Stack>
    );
  }

  if (error || !data) {
    return (
      <Stack spacing={2}>
        {backButton}
        <Alert severity="error">과정 정보를 불러오지 못했습니다.</Alert>
      </Stack>
    );
  }

  const sessions = data.sessions ?? [];

  return (
    <Stack spacing={2}>
      {backButton}
      <Stack direction="row" spacing={1} alignItems="center">
        <Typography variant="h6" component="h2">
          {data.title}
        </Typography>
        <Chip size="small" label={data.category} />
        <Chip size="small" label={statusLabel(data.status)} color={data.status === 'OPEN' ? 'success' : 'default'} />
      </Stack>
      {data.description && <Typography color="text.secondary">{data.description}</Typography>}

      <Typography variant="subtitle1" component="h3">
        회차
      </Typography>
      {sessions.length === 0 ? (
        <Typography color="text.secondary">등록된 회차가 없습니다.</Typography>
      ) : (
        <Table aria-label="회차 목록">
          <TableHead>
            <TableRow>
              <TableCell>일정</TableCell>
              <TableCell>장소</TableCell>
              <TableCell align="right">정원</TableCell>
              <TableCell align="right">남은 자리</TableCell>
              <TableCell>상태</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sessions.map((s) => (
              <TableRow key={s.id}>
                <TableCell>{s.startDate}</TableCell>
                <TableCell>{s.place}</TableCell>
                <TableCell align="right">{s.capacity}</TableCell>
                <TableCell align="right">{s.remainingSeats}</TableCell>
                <TableCell>{statusLabel(s.status)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Stack>
  );
}

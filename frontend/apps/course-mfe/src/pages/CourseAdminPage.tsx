import { useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Chip,
  Divider,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useCourseAdmin } from '../hooks/useCourseAdmin';

const CATEGORIES = ['BACKEND', 'FRONTEND'];
const EMPTY_COURSE = { title: '', category: CATEGORIES[0], description: '' };
const EMPTY_SESSION = { startDate: '', place: '', capacity: '' };

/**
 * 과정 관리 화면 (FR-03): 과정 등록·수정, 회차 추가, 과정 종료.
 * 데이터는 useCourseAdmin만 쓰고 courseApi를 직접 부르지 않는다. 입력 검증은 서버(400)에 맡기고 그 message를 보여 준다.
 */
export function CourseAdminPage() {
  const { course, pending, error, loadCourse, createCourse, updateCourse, addSession, closeCourse, reset } =
    useCourseAdmin();
  const [loadId, setLoadId] = useState('');
  const [form, setForm] = useState(EMPTY_COURSE);
  const [sessionForm, setSessionForm] = useState(EMPTY_SESSION);
  const [notice, setNotice] = useState<string>();

  // 불러오거나 저장한 과정 내용을 입력란에 채운다
  useEffect(() => {
    setForm(
      course
        ? { title: course.title ?? '', category: course.category ?? CATEGORIES[0], description: course.description ?? '' }
        : EMPTY_COURSE,
    );
  }, [course]);

  const done = (ok: boolean, message: string) => setNotice(ok ? message : undefined);

  const handleSaveCourse = async () => {
    const request = { title: form.title, category: form.category, description: form.description || undefined };
    if (course?.id != null) done(await updateCourse(course.id, request), '과정을 수정했습니다.');
    else done(await createCourse(request), '과정을 등록했습니다.');
  };

  const handleAddSession = async () => {
    if (course?.id == null) return;
    const ok = await addSession(course.id, {
      // 빈 날짜는 ""가 아니라 값 없이 보내야 서버가 날짜 파싱 오류 대신 VALIDATION_FAILED(400)로 답한다
      startDate: sessionForm.startDate || (undefined as unknown as string),
      place: sessionForm.place,
      // 빈 정원은 0으로 보내 서버의 정원 검증(1 이상) 메시지를 받는다
      capacity: Number(sessionForm.capacity) || 0,
    });
    done(ok, '회차를 추가했습니다.');
    if (ok) setSessionForm(EMPTY_SESSION);
  };

  const handleClose = async () => {
    if (course?.id == null) return;
    done(await closeCourse(course.id), '과정을 종료했습니다.');
  };

  const handleLoad = async () => {
    const id = Number(loadId);
    if (!Number.isInteger(id) || id <= 0) return;
    done(await loadCourse(id), '과정을 불러왔습니다.');
  };

  const handleNew = () => {
    reset();
    setNotice(undefined);
    setSessionForm(EMPTY_SESSION);
  };

  return (
    <Stack spacing={3}>
      <Stack direction="row" spacing={2} alignItems="center">
        <TextField
          label="과정 ID"
          size="small"
          type="number"
          value={loadId}
          onChange={(e) => setLoadId(e.target.value)}
        />
        <Button variant="outlined" onClick={handleLoad} disabled={pending || !loadId}>
          불러오기
        </Button>
        <Button onClick={handleNew} disabled={pending}>
          새 과정
        </Button>
      </Stack>

      {error && <Alert severity="error">{error}</Alert>}
      {!error && notice && <Alert severity="success">{notice}</Alert>}

      <Stack spacing={2} component="section" aria-label="과정 정보">
        <Stack direction="row" spacing={1} alignItems="center">
          <Typography variant="h6" component="h2">
            {course ? `과정 수정 (ID ${course.id})` : '과정 등록'}
          </Typography>
          {course?.status && <Chip size="small" label={course.status} />}
        </Stack>
        <TextField label="과정명" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        <TextField
          select
          label="분류"
          value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value })}
          sx={{ maxWidth: 240 }}
        >
          {CATEGORIES.map((c) => (
            <MenuItem key={c} value={c}>
              {c}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          label="설명"
          multiline
          minRows={2}
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
        <Stack direction="row" spacing={2}>
          <Button variant="contained" onClick={handleSaveCourse} disabled={pending}>
            {course ? '수정' : '등록'}
          </Button>
          {course && (
            <Button color="error" variant="outlined" onClick={handleClose} disabled={pending}>
              과정 종료
            </Button>
          )}
        </Stack>
      </Stack>

      {course && (
        <>
          <Divider />
          <Stack spacing={2} component="section" aria-label="회차">
            <Typography variant="h6" component="h2">
              회차
            </Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>시작일</TableCell>
                  <TableCell>장소</TableCell>
                  <TableCell>정원</TableCell>
                  <TableCell>남은 자리</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {course.sessions?.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell>{s.startDate}</TableCell>
                    <TableCell>{s.place}</TableCell>
                    <TableCell>{s.capacity}</TableCell>
                    <TableCell>{s.remainingSeats}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <Stack direction="row" spacing={2}>
              <TextField
                label="시작일"
                type="date"
                value={sessionForm.startDate}
                onChange={(e) => setSessionForm({ ...sessionForm, startDate: e.target.value })}
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                label="장소"
                value={sessionForm.place}
                onChange={(e) => setSessionForm({ ...sessionForm, place: e.target.value })}
              />
              <TextField
                label="정원"
                type="number"
                value={sessionForm.capacity}
                onChange={(e) => setSessionForm({ ...sessionForm, capacity: e.target.value })}
                sx={{ maxWidth: 120 }}
              />
              <Button variant="contained" onClick={handleAddSession} disabled={pending}>
                회차 추가
              </Button>
            </Stack>
          </Stack>
        </>
      )}
    </Stack>
  );
}

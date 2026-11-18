import { act, renderHook } from '@testing-library/react';
import { courseApi } from '../api/courseApi';
import { toErrorMessage, useCourseAdmin } from './useCourseAdmin';

jest.mock('../api/courseApi', () => ({
  courseApi: {
    getCourse: jest.fn(),
    createCourse: jest.fn(),
    updateCourse: jest.fn(),
    addSession: jest.fn(),
    closeCourse: jest.fn(),
  },
}));
const api = courseApi as unknown as Record<string, jest.Mock>;

// 생성 클라이언트(axios)가 던지는 오류 모양: response.status, response.data(ErrorResponse)
const apiError = (status: number, code: string, message: string) =>
  Object.assign(new Error(`Request failed with status code ${status}`), {
    isAxiosError: true,
    response: { status, data: { code, message } },
  });

const course = { id: 1, title: 'Spring Boot 입문', category: 'BACKEND', status: 'OPEN', sessions: [] };

beforeEach(() => Object.values(api).forEach((m) => m.mockReset()));

test('과정을 등록하면 응답을 course로 돌려준다', async () => {
  api.createCourse.mockResolvedValue({ data: course });
  const { result } = renderHook(() => useCourseAdmin());

  let ok = false;
  await act(async () => {
    ok = await result.current.createCourse({ title: 'Spring Boot 입문', category: 'BACKEND' });
  });

  expect(ok).toBe(true);
  expect(api.createCourse).toHaveBeenCalledWith({ title: 'Spring Boot 입문', category: 'BACKEND' });
  expect(result.current.course).toEqual(course);
  expect(result.current.error).toBeUndefined();
});

test('과정 수정·회차 추가·종료는 과정 id로 호출하고 결과를 반영한다', async () => {
  api.createCourse.mockResolvedValue({ data: course });
  api.updateCourse.mockResolvedValue({ data: { ...course, title: 'Spring Boot 심화' } });
  api.addSession.mockResolvedValue({
    data: { id: 10, startDate: '2026-12-01', place: '본사 3층', capacity: 20, remainingSeats: 20, status: 'OPEN' },
  });
  api.closeCourse.mockResolvedValue({ data: { ...course, title: 'Spring Boot 심화', status: 'CLOSED' } });
  const { result } = renderHook(() => useCourseAdmin());

  await act(async () => {
    await result.current.createCourse({ title: 'Spring Boot 입문', category: 'BACKEND' });
  });
  await act(async () => {
    await result.current.updateCourse(1, { title: 'Spring Boot 심화', category: 'BACKEND' });
  });
  expect(api.updateCourse).toHaveBeenCalledWith(1, { title: 'Spring Boot 심화', category: 'BACKEND' });
  expect(result.current.course?.title).toBe('Spring Boot 심화');

  await act(async () => {
    await result.current.addSession(1, { startDate: '2026-12-01', place: '본사 3층', capacity: 20 });
  });
  expect(api.addSession).toHaveBeenCalledWith(1, { startDate: '2026-12-01', place: '본사 3층', capacity: 20 });
  expect(result.current.course?.sessions).toHaveLength(1);

  await act(async () => {
    await result.current.closeCourse(1);
  });
  expect(api.closeCourse).toHaveBeenCalledWith(1);
  expect(result.current.course?.status).toBe('CLOSED');
});

test('입력 오류(400 VALIDATION_FAILED)면 서버 message를 error로 돌려준다', async () => {
  api.createCourse.mockRejectedValue(apiError(400, 'VALIDATION_FAILED', 'title: 공백일 수 없습니다'));
  const { result } = renderHook(() => useCourseAdmin());

  let ok = true;
  await act(async () => {
    ok = await result.current.createCourse({ title: '', category: 'BACKEND' });
  });

  expect(ok).toBe(false);
  expect(result.current.error).toBe('title: 공백일 수 없습니다');
  expect(result.current.course).toBeUndefined();
  expect(result.current.pending).toBe(false);
});

test('종료된 과정 변경(409 COURSE_CLOSED)이면 서버 message를 error로 돌려준다', async () => {
  api.addSession.mockRejectedValue(apiError(409, 'COURSE_CLOSED', '종료된 과정은 변경할 수 없습니다.'));
  const { result } = renderHook(() => useCourseAdmin());

  let ok = true;
  await act(async () => {
    ok = await result.current.addSession(1, { startDate: '2026-12-01', place: '본사', capacity: 10 });
  });

  expect(ok).toBe(false);
  expect(result.current.error).toBe('종료된 과정은 변경할 수 없습니다.');
});

test('서버 message가 없는 오류는 기본 문구로 바꾼다', () => {
  expect(toErrorMessage(new Error('Network Error'))).toBe('요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.');
  expect(toErrorMessage(undefined)).toBe('요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.');
});

import { renderHook, waitFor } from '@testing-library/react';
import { courseApi } from '../api/courseApi';
import { useCourse } from './useCourse';

jest.mock('../api/courseApi', () => ({ courseApi: { getCourse: jest.fn() } }));
const getCourse = courseApi.getCourse as jest.Mock;

const detail = {
  id: 1,
  title: 'Spring Boot 입문',
  category: 'BACKEND',
  description: '스프링 부트 기초',
  status: 'OPEN',
  sessions: [{ id: 10, startDate: '2026-12-01', place: '본사 3층', capacity: 20, remainingSeats: 20, status: 'OPEN' }],
};

beforeEach(() => getCourse.mockReset());

test('과정 상세를 불러와 data로 돌려준다', async () => {
  getCourse.mockResolvedValue({ data: detail });

  const { result } = renderHook(() => useCourse(1));

  expect(result.current.loading).toBe(true);
  await waitFor(() => expect(result.current.loading).toBe(false));
  expect(getCourse).toHaveBeenCalledWith(1);
  expect(result.current.data?.sessions?.[0].place).toBe('본사 3층');
  expect(result.current.error).toBeUndefined();
  expect(result.current.notFound).toBe(false);
});

test('없는 과정(404)이면 error 대신 notFound를 돌려준다', async () => {
  getCourse.mockRejectedValue({ isAxiosError: true, response: { status: 404 } });

  const { result } = renderHook(() => useCourse(999));

  await waitFor(() => expect(result.current.loading).toBe(false));
  expect(result.current.notFound).toBe(true);
  expect(result.current.error).toBeUndefined();
  expect(result.current.data).toBeUndefined();
});

test('그 밖의 실패는 error로 돌려준다', async () => {
  getCourse.mockRejectedValue(new Error('Network Error'));

  const { result } = renderHook(() => useCourse(1));

  await waitFor(() => expect(result.current.loading).toBe(false));
  expect(result.current.error).toBeInstanceOf(Error);
  expect(result.current.notFound).toBe(false);
});

test('id가 바뀌면 새 과정을 다시 불러온다', async () => {
  getCourse.mockResolvedValue({ data: detail });

  const { result, rerender } = renderHook(({ id }) => useCourse(id), { initialProps: { id: 1 } });
  await waitFor(() => expect(result.current.loading).toBe(false));

  getCourse.mockResolvedValue({ data: { ...detail, id: 2, title: 'JPA 실전' } });
  rerender({ id: 2 });

  await waitFor(() => expect(result.current.data?.title).toBe('JPA 실전'));
  expect(getCourse).toHaveBeenLastCalledWith(2);
});

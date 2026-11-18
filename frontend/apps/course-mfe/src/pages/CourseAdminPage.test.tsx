import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { useCourseAdmin } from '../hooks/useCourseAdmin';
import { CourseAdminPage } from './CourseAdminPage';

// 화면 테스트는 훅을 mock한다. API 호출과 오류 message 추출은 useCourseAdmin.test.ts가 따로 검증한다
jest.mock('../hooks/useCourseAdmin');
const mockUseCourseAdmin = useCourseAdmin as jest.Mock;

const course = {
  id: 1,
  title: 'Spring Boot 입문',
  category: 'BACKEND',
  description: '',
  status: 'OPEN',
  sessions: [{ id: 10, startDate: '2026-12-01', place: '본사 3층', capacity: 20, remainingSeats: 20, status: 'OPEN' }],
};

const hookState = (overrides: Partial<ReturnType<typeof useCourseAdmin>> = {}) => ({
  course: undefined,
  pending: false,
  error: undefined,
  loadCourse: jest.fn().mockResolvedValue(true),
  createCourse: jest.fn().mockResolvedValue(true),
  updateCourse: jest.fn().mockResolvedValue(true),
  addSession: jest.fn().mockResolvedValue(true),
  closeCourse: jest.fn().mockResolvedValue(true),
  reset: jest.fn(),
  ...overrides,
});

test('입력한 내용으로 과정을 등록한다', async () => {
  const state = hookState();
  mockUseCourseAdmin.mockReturnValue(state);

  render(<CourseAdminPage />);
  fireEvent.change(screen.getByLabelText('과정명'), { target: { value: 'Spring Boot 입문' } });
  fireEvent.change(screen.getByLabelText('설명'), { target: { value: '입문 과정' } });
  fireEvent.click(screen.getByRole('button', { name: '등록' }));

  await waitFor(() =>
    expect(state.createCourse).toHaveBeenCalledWith({
      title: 'Spring Boot 입문',
      category: 'BACKEND',
      description: '입문 과정',
    }),
  );
  expect(await screen.findByText('과정을 등록했습니다.')).toBeInTheDocument();
});

test('과정이 있으면 수정·회차 추가·종료를 과정 id로 호출한다', async () => {
  const state = hookState({ course });
  mockUseCourseAdmin.mockReturnValue(state);

  render(<CourseAdminPage />);
  expect(screen.getByText('본사 3층')).toBeInTheDocument();

  fireEvent.change(screen.getByLabelText('과정명'), { target: { value: 'Spring Boot 심화' } });
  fireEvent.click(screen.getByRole('button', { name: '수정' }));
  await waitFor(() =>
    expect(state.updateCourse).toHaveBeenCalledWith(1, { title: 'Spring Boot 심화', category: 'BACKEND' }),
  );

  fireEvent.change(screen.getByLabelText('시작일'), { target: { value: '2027-01-05' } });
  fireEvent.change(screen.getByLabelText('장소'), { target: { value: '연수원' } });
  fireEvent.change(screen.getByLabelText('정원'), { target: { value: '30' } });
  fireEvent.click(screen.getByRole('button', { name: '회차 추가' }));
  await waitFor(() =>
    expect(state.addSession).toHaveBeenCalledWith(1, { startDate: '2027-01-05', place: '연수원', capacity: 30 }),
  );

  fireEvent.click(screen.getByRole('button', { name: '과정 종료' }));
  await waitFor(() => expect(state.closeCourse).toHaveBeenCalledWith(1));
});

test('입력 오류(400)면 서버 message를 보여 준다', () => {
  mockUseCourseAdmin.mockReturnValue(hookState({ error: 'title: 공백일 수 없습니다' }));

  render(<CourseAdminPage />);

  expect(screen.getByRole('alert')).toHaveTextContent('title: 공백일 수 없습니다');
});

test('종료된 과정 변경(409)이면 서버 message를 보여 준다', () => {
  mockUseCourseAdmin.mockReturnValue(
    hookState({ course: { ...course, status: 'CLOSED' }, error: '종료된 과정은 변경할 수 없습니다.' }),
  );

  render(<CourseAdminPage />);

  expect(screen.getByRole('alert')).toHaveTextContent('종료된 과정은 변경할 수 없습니다.');
});

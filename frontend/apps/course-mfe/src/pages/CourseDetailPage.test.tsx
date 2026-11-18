import { fireEvent, render, screen, within } from '@testing-library/react';
import { useCourse } from '../hooks/useCourse';
import { CourseDetailPage } from './CourseDetailPage';

// 화면 테스트는 훅을 mock한다. API 호출 방식은 useCourse.test.ts가 따로 검증한다
jest.mock('../hooks/useCourse');
const mockUseCourse = useCourse as jest.Mock;

const loaded = (data: object) => ({ data, loading: false, error: undefined, notFound: false });

test('과정 정보와 회차(일정, 장소, 정원, 남은 자리, 상태)를 보여 준다', () => {
  mockUseCourse.mockReturnValue(
    loaded({
      id: 1,
      title: 'Spring Boot 입문',
      category: 'BACKEND',
      description: '스프링 부트 기초',
      status: 'OPEN',
      sessions: [{ id: 10, startDate: '2026-12-01', place: '본사 3층', capacity: 20, remainingSeats: 15, status: 'CLOSED' }],
    }),
  );

  render(<CourseDetailPage courseId={1} onBack={jest.fn()} />);

  expect(mockUseCourse).toHaveBeenCalledWith(1);
  expect(screen.getByRole('heading', { name: 'Spring Boot 입문' })).toBeInTheDocument();
  expect(screen.getByText('스프링 부트 기초')).toBeInTheDocument();
  const row = within(screen.getByRole('table', { name: '회차 목록' })).getAllByRole('row')[1];
  expect(within(row).getAllByRole('cell').map((c) => c.textContent)).toEqual([
    '2026-12-01',
    '본사 3층',
    '20',
    '15',
    '종료',
  ]);
});

test('회차가 없으면 안내 문구를 보여 준다', () => {
  mockUseCourse.mockReturnValue(loaded({ id: 1, title: 'JPA 실전', status: 'OPEN', sessions: [] }));

  render(<CourseDetailPage courseId={1} onBack={jest.fn()} />);

  expect(screen.getByText('등록된 회차가 없습니다.')).toBeInTheDocument();
});

test('없는 과정(404)이면 안내 문구를 보여 준다', () => {
  mockUseCourse.mockReturnValue({ data: undefined, loading: false, error: undefined, notFound: true });

  render(<CourseDetailPage courseId={999} onBack={jest.fn()} />);

  expect(screen.getByRole('alert')).toHaveTextContent('해당 과정을 찾을 수 없습니다.');
});

test('불러오기에 실패하면 에러 메시지를 보여 준다', () => {
  mockUseCourse.mockReturnValue({ data: undefined, loading: false, error: new Error('fail'), notFound: false });

  render(<CourseDetailPage courseId={1} onBack={jest.fn()} />);

  expect(screen.getByRole('alert')).toHaveTextContent('과정 정보를 불러오지 못했습니다.');
});

test('목록으로 버튼을 누르면 onBack을 부른다', () => {
  mockUseCourse.mockReturnValue({ data: undefined, loading: false, error: undefined, notFound: true });
  const onBack = jest.fn();

  render(<CourseDetailPage courseId={999} onBack={onBack} />);
  fireEvent.click(screen.getByRole('button', { name: /목록으로/ }));

  expect(onBack).toHaveBeenCalled();
});

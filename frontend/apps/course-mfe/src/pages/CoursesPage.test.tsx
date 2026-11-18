import { fireEvent, render, screen } from '@testing-library/react';
import { useCourse } from '../hooks/useCourse';
import { useCourses } from '../hooks/useCourses';
import { CoursesPage } from './CoursesPage';

jest.mock('../hooks/useCourses');
jest.mock('../hooks/useCourse');
const mockUseCourses = useCourses as jest.Mock;
const mockUseCourse = useCourse as jest.Mock;

beforeEach(() => {
  mockUseCourses.mockReturnValue({
    data: {
      content: [{ id: 7, title: 'Spring Boot 입문', category: 'BACKEND', status: 'OPEN', sessionCount: 1 }],
      page: 0,
      size: 10,
      totalElements: 1,
    },
    loading: false,
    error: undefined,
  });
  mockUseCourse.mockReturnValue({
    data: { id: 7, title: 'Spring Boot 입문 상세', status: 'OPEN', sessions: [] },
    loading: false,
    error: undefined,
    notFound: false,
  });
});

test('목록에서 과정을 누르면 상세로 가고, 목록으로를 누르면 돌아온다', () => {
  render(<CoursesPage />);

  fireEvent.click(screen.getByText('Spring Boot 입문'));

  expect(mockUseCourse).toHaveBeenCalledWith(7);
  expect(screen.getByRole('heading', { name: 'Spring Boot 입문 상세' })).toBeInTheDocument();
  expect(screen.queryByRole('table')).not.toBeInTheDocument(); // 숨긴 목록 표는 접근성 트리에서 빠진다

  fireEvent.click(screen.getByRole('button', { name: /목록으로/ }));

  expect(screen.queryByRole('heading', { name: 'Spring Boot 입문 상세' })).not.toBeInTheDocument();
  expect(screen.getByRole('table')).toBeInTheDocument();
});

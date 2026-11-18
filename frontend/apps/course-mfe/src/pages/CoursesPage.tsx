import { useState } from 'react';
import { CourseDetailPage } from './CourseDetailPage';
import { CourseListPage } from './CourseListPage';

/**
 * 과정 목록(FR-01)과 상세(FR-02)를 오가는 화면. 라우터 없이 선택한 과정 id 상태로만 전환한다.
 * 목록은 상세를 보는 동안에도 숨겨 둔 채 유지해, 돌아왔을 때 검색어·페이지가 그대로 남는다.
 */
export function CoursesPage() {
  const [selectedId, setSelectedId] = useState<number>();

  return (
    <>
      <div hidden={selectedId !== undefined}>
        <CourseListPage onSelect={setSelectedId} />
      </div>
      {selectedId !== undefined && (
        <CourseDetailPage courseId={selectedId} onBack={() => setSelectedId(undefined)} />
      )}
    </>
  );
}

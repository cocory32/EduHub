import { useEffect, useState } from 'react';
import type { CourseDetailResponse } from '@eduhub/api-client';
import { courseApi } from '../api/courseApi';

/** 생성 클라이언트(Axios)가 던진 에러가 404 응답인지. axios를 직접 import하지 않고 응답 모양만 본다 */
function isNotFound(e: unknown): boolean {
  return (e as { response?: { status?: number } } | undefined)?.response?.status === 404;
}

/**
 * 과정 상세와 회차 목록(FR-02)을 불러오는 훅. 화면은 이 훅의 값만 쓴다.
 * 없는 과정(404)은 error 대신 notFound로 구분해 돌려준다.
 */
export function useCourse(id: number) {
  const [data, setData] = useState<CourseDetailResponse>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>();
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let ignore = false; // 다른 과정으로 빠르게 바꿀 때, 늦게 도착한 이전 요청의 응답은 버린다
    setLoading(true);
    setError(undefined);
    setNotFound(false);
    setData(undefined);

    courseApi
      .getCourse(id)
      .then((response) => {
        if (!ignore) setData(response.data);
      })
      .catch((e: unknown) => {
        if (ignore) return;
        if (isNotFound(e)) setNotFound(true);
        else setError(e);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [id]);

  return { data, loading, error, notFound };
}

import { useCallback, useState } from 'react';
import type { CourseDetailResponse, CourseRequest, ErrorResponse, SessionRequest } from '@eduhub/api-client';
import { courseApi } from '../api/courseApi';

const FALLBACK_MESSAGE = '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.';

/** 오류 응답(ErrorResponse)의 message를 꺼낸다. 400 VALIDATION_FAILED, 409 COURSE_CLOSED 등 서버 메시지를 그대로 보여 준다. */
export function toErrorMessage(e: unknown): string {
  // 생성 클라이언트가 던지는 AxiosError의 response.data가 ErrorResponse다 (axios를 직접 의존하지 않으려고 모양만 본다)
  const data = (e as { response?: { data?: ErrorResponse } } | null)?.response?.data;
  return typeof data?.message === 'string' && data.message ? data.message : FALLBACK_MESSAGE;
}

/**
 * 과정 관리(FR-03) 훅: 과정 등록·수정, 회차 추가, 과정 종료.
 * 화면은 이 훅의 course·pending·error와 동작 함수만 쓴다. 동작 함수는 성공 여부(boolean)를 돌려준다.
 */
export function useCourseAdmin() {
  const [course, setCourse] = useState<CourseDetailResponse>();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string>();

  const run = useCallback(async (call: () => Promise<void>): Promise<boolean> => {
    setPending(true);
    setError(undefined);
    try {
      await call();
      return true;
    } catch (e: unknown) {
      setError(toErrorMessage(e));
      return false;
    } finally {
      setPending(false);
    }
  }, []);

  const loadCourse = useCallback(
    (id: number) =>
      run(async () => {
        setCourse((await courseApi.getCourse(id)).data);
      }),
    [run],
  );

  const createCourse = useCallback(
    (request: CourseRequest) =>
      run(async () => {
        setCourse((await courseApi.createCourse(request)).data);
      }),
    [run],
  );

  const updateCourse = useCallback(
    (id: number, request: CourseRequest) =>
      run(async () => {
        setCourse((await courseApi.updateCourse(id, request)).data);
      }),
    [run],
  );

  const addSession = useCallback(
    (id: number, request: SessionRequest) =>
      run(async () => {
        const session = (await courseApi.addSession(id, request)).data;
        setCourse((prev) => (prev ? { ...prev, sessions: [...(prev.sessions ?? []), session] } : prev));
      }),
    [run],
  );

  const closeCourse = useCallback(
    (id: number) =>
      run(async () => {
        setCourse((await courseApi.closeCourse(id)).data);
      }),
    [run],
  );

  /** 새 과정 입력으로 돌아간다. */
  const reset = useCallback(() => {
    setCourse(undefined);
    setError(undefined);
  }, []);

  return { course, pending, error, loadCourse, createCourse, updateCourse, addSession, closeCourse, reset };
}

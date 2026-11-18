-- FR-04~06: 수강 신청. 상태는 CONFIRMED(확정), WAITING(대기), CANCELED(취소), COMPLETED(수료).
CREATE TABLE enrollment (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT      NOT NULL REFERENCES course_session (id),
    employee_id BIGINT      NOT NULL,
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    canceled_at TIMESTAMP WITH TIME ZONE
);

-- FR-04: 같은 회차에 진행 중인(확정·대기) 신청은 한 사람당 하나. 취소 후 재신청은 허용한다.
CREATE UNIQUE INDEX uq_enrollment_active_session_employee
    ON enrollment (session_id, employee_id)
    WHERE status IN ('CONFIRMED', 'WAITING');

-- FR-05/06: 회차별 상태 집계와 대기 순번(id 순) 조회
CREATE INDEX idx_enrollment_session_status_id ON enrollment (session_id, status, id);

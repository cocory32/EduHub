package com.example.eduhub.enrollment.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.eduhub.common.error.BusinessException;
import com.example.eduhub.course.domain.Course;
import com.example.eduhub.course.domain.CourseRepository;
import com.example.eduhub.enrollment.domain.EnrollmentRepository;
import com.example.eduhub.enrollment.domain.EnrollmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * NFR-01: 실제 PostgreSQL(.env의 TEST_DB_*)에서 동시 신청을 실행한다. 트랜잭션이 스레드마다 따로 커밋되므로 롤백 대신 끝나고 직접 지운다.
 *
 * <p>CourseSessionRepository.findByIdForUpdate의 @Lock을 지우면 이 테스트가 실패해야 한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class EnrollmentConcurrencyTest {

  private static final int THREADS = 100;

  @Autowired private EnrollmentService enrollmentService;
  @Autowired private EnrollmentRepository enrollmentRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private Long courseId;

  @BeforeEach
  void setUp() {
    Course course = new Course("동시성 테스트", "TEST", null);
    course.addSession(LocalDate.now().plusDays(30), "본사 3층", 10); // 정원 10
    courseId = courseRepository.save(course).getId();
  }

  @AfterEach
  void tearDown() {
    jdbcTemplate.update(
        "delete from enrollment where session_id in (select id from course_session where course_id = ?)",
        courseId);
    jdbcTemplate.update("delete from course_session where course_id = ?", courseId);
    jdbcTemplate.update("delete from course where id = ?", courseId);
  }

  private Long sessionId() {
    return jdbcTemplate.queryForObject(
        "select id from course_session where course_id = ?", Long.class, courseId);
  }

  /** 모든 작업을 한꺼번에 출발시키고, 실패는 오류 코드별로 센다. */
  private Map<String, AtomicInteger> runConcurrently(List<Runnable> tasks)
      throws InterruptedException {
    Map<String, AtomicInteger> failures = new ConcurrentHashMap<>();
    ExecutorService pool = Executors.newFixedThreadPool(32);
    CountDownLatch ready = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(tasks.size());
    for (Runnable task : tasks) {
      pool.submit(
          () -> {
            try {
              ready.await();
              task.run();
            } catch (BusinessException e) {
              failures.computeIfAbsent(e.getCode(), k -> new AtomicInteger()).incrementAndGet();
            } catch (Exception e) {
              failures
                  .computeIfAbsent(e.getClass().getSimpleName(), k -> new AtomicInteger())
                  .incrementAndGet();
            } finally {
              done.countDown();
            }
          });
    }
    ready.countDown();
    assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
    pool.shutdown();
    return failures;
  }

  @Test
  @DisplayName("NFR-01: 100명이 동시에 신청해도 확정은 정원(10)을 넘지 않는다")
  void concurrentEnroll_neverExceedsCapacity() throws InterruptedException {
    Long sessionId = sessionId();
    List<Runnable> tasks =
        java.util.stream.LongStream.rangeClosed(1, THREADS)
            .<Runnable>mapToObj(employee -> () -> enrollmentService.enroll(sessionId, employee))
            .toList();

    Map<String, AtomicInteger> failures = runConcurrently(tasks);

    assertThat(
            enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.CONFIRMED))
        .isEqualTo(10);
    assertThat(failures.keySet()).containsOnly("SESSION_FULL");
    assertThat(failures.get("SESSION_FULL").get()).isEqualTo(THREADS - 10);
  }

  @Test
  @DisplayName("FR-04: 같은 사람이 동시에 여러 번 신청해도 진행 중인 신청은 하나")
  void concurrentDuplicate_onlyOne() throws InterruptedException {
    Long sessionId = sessionId();
    List<Runnable> tasks =
        java.util.stream.IntStream.range(0, 10)
            .<Runnable>mapToObj(i -> () -> enrollmentService.enroll(sessionId, 1L))
            .toList();

    Map<String, AtomicInteger> failures = runConcurrently(tasks);

    assertThat(
            enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.CONFIRMED))
        .isEqualTo(1);
    assertThat(failures.get("DUPLICATE_ENROLLMENT").get()).isEqualTo(9);
  }
}

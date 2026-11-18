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
 * NFR-01: 실제 PostgreSQL(.env의 TEST_DB_*)에서 동시 신청·취소를 실행한다. 트랜잭션이 스레드마다 따로 커밋되므로 롤백 대신 끝나고 직접 지운다.
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
    course.addSession(LocalDate.now().plusDays(30), "본사 3층", 10); // 정원 10, 대기 5
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
  @DisplayName("NFR-01: 100명이 동시에 신청해도 확정은 정원(10), 대기는 50%(5)를 넘지 않는다")
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
    assertThat(enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.WAITING))
        .isEqualTo(5);
    assertThat(failures.keySet()).containsOnly("WAITLIST_FULL");
    assertThat(failures.get("WAITLIST_FULL").get()).isEqualTo(THREADS - 15);
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

  @Test
  @DisplayName("FR-06: 확정자 전원이 동시에 취소하면 대기자 5명이 모두 승급되고 정원을 넘지 않는다")
  void concurrentCancel_promotesWithoutOverflow() throws InterruptedException {
    Long sessionId = sessionId();
    List<Long> confirmedIds =
        java.util.stream.LongStream.rangeClosed(1, 15)
            .mapToObj(employee -> enrollmentService.enroll(sessionId, employee))
            .filter(r -> r.status().equals("CONFIRMED"))
            .map(r -> r.id())
            .toList();
    assertThat(confirmedIds).hasSize(10);

    List<Runnable> tasks =
        java.util.stream.IntStream.range(0, confirmedIds.size())
            .<Runnable>mapToObj(
                i -> () -> enrollmentService.cancel(confirmedIds.get(i), (long) (i + 1)))
            .toList();

    Map<String, AtomicInteger> failures = runConcurrently(tasks);

    assertThat(failures).isEmpty();
    assertThat(
            enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.CONFIRMED))
        .isEqualTo(5);
    assertThat(enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.WAITING))
        .isZero();
    assertThat(enrollmentRepository.countBySessionIdAndStatus(sessionId, EnrollmentStatus.CANCELED))
        .isEqualTo(10);
  }
}

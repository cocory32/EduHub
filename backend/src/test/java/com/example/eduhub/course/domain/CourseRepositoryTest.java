package com.example.eduhub.course.domain;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

/** 실제 PostgreSQL(.env의 TEST_DB_*)에서 검색 쿼리를 확인한다. 각 테스트는 끝나면 롤백된다. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CourseRepositoryTest {

  @Autowired private CourseRepository courseRepository;

  @Autowired private EntityManager entityManager;

  private Course spring;

  @BeforeEach
  void setUp() {
    spring = new Course("Spring Boot 입문", "BACKEND", null);
    spring.addSession(LocalDate.of(2026, 12, 1), "본사 3층", 20);
    spring.addSession(LocalDate.of(2026, 12, 8), "본사 3층", 20);
    courseRepository.save(spring);
    courseRepository.save(new Course("JPA 실전", "BACKEND", null));
    courseRepository.save(new Course("React 기초", "FRONTEND", null));
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("제목 일부로 대소문자 구분 없이 검색한다")
  void search_byKeyword() {
    Page<Course> page = courseRepository.search("spring", "", PageRequest.of(0, 10));

    assertThat(page.getContent())
        .extracting(Course::getTitle)
        .contains("Spring Boot 입문")
        .doesNotContain("JPA 실전", "React 기초");
  }

  @Test
  @DisplayName("분류로 검색한다")
  void search_byCategory() {
    Page<Course> page = courseRepository.search("", "FRONTEND", PageRequest.of(0, 100));

    assertThat(page.getContent()).extracting(Course::getCategory).containsOnly("FRONTEND");
    assertThat(page.getContent()).extracting(Course::getTitle).contains("React 기초");
  }

  @Test
  @DisplayName("과정별 회차 수를 한 번에 센다")
  void countSessionsByCourseIds() {
    List<Object[]> rows = courseRepository.countSessionsByCourseIds(List.of(spring.getId()));

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0)[1]).isEqualTo(2L);
  }
}

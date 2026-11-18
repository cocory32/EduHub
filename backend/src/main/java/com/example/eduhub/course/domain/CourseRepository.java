package com.example.eduhub.course.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

  /**
   * 제목(부분 일치, 대소문자 무시)과 분류(정확히 일치)로 검색한다. 조건을 쓰지 않을 때는 빈 문자열을 넘긴다. null을 넘기면 PostgreSQL이 파라미터 타입을
   * 추론하지 못하는 경우가 있어서 빈 문자열을 쓴다.
   */
  @Query(
      value =
          """
          select c from Course c
          where lower(c.title) like lower(concat('%', :keyword, '%'))
            and (:category = '' or c.category = :category)
          """,
      countQuery =
          """
          select count(c) from Course c
          where lower(c.title) like lower(concat('%', :keyword, '%'))
            and (:category = '' or c.category = :category)
          """)
  Page<Course> search(
      @Param("keyword") String keyword, @Param("category") String category, Pageable pageable);

  /** 과정별 회차 수. 목록 화면에서 과정마다 회차를 지연 로딩하지 않도록(N+1 방지, NFR-02) 한 번에 센다. */
  @Query(
      """
      select s.course.id, count(s) from CourseSession s
      where s.course.id in :courseIds
      group by s.course.id
      """)
  List<Object[]> countSessionsByCourseIds(@Param("courseIds") Collection<Long> courseIds);

  /** 상세 조회용. 회차를 함께 가져온다. */
  @Query("select c from Course c left join fetch c.sessions where c.id = :id")
  Optional<Course> findWithSessionsById(@Param("id") Long id);
}

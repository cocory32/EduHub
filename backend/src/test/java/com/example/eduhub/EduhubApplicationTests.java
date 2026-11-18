package com.example.eduhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** test 프로필로 전체 애플리케이션이 뜨고 테스트 DB 연결과 Flyway가 동작하는지 확인한다. */
@SpringBootTest
@ActiveProfiles("test")
class EduhubApplicationTests {

  @Test
  void contextLoads() {}
}

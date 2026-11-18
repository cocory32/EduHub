package com.example.eduhub.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 현재 시각을 주입받게 해서 날짜 규칙(예: 취소 기한)을 테스트에서 고정할 수 있게 한다. */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}

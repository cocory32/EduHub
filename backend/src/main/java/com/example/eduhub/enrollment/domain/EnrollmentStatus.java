package com.example.eduhub.enrollment.domain;

import java.util.Set;

/** 신청 상태. CONFIRMED와 WAITING이 진행 중인 신청이다. */
public enum EnrollmentStatus {
  CONFIRMED,
  WAITING,
  CANCELED,
  COMPLETED;

  public static final Set<EnrollmentStatus> ACTIVE = Set.of(CONFIRMED, WAITING);

  public boolean isActive() {
    return ACTIVE.contains(this);
  }
}

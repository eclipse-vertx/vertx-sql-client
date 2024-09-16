/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient.data;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.MICROS;
import static org.junit.Assert.assertEquals;

public class IntervalTest {

  @Test
  public void testFromDuration() {
    assertEquals(Interval.of(), Interval.of(Duration.ZERO));

    assertEquals(
      Interval.of(0, 0, 0, 0, 0, -1, 999999),
      Interval.of(Duration.ZERO.minus(1, MICROS)));

    assertEquals(
      Interval.of(0, 0, -15, 0, -12, -3),
      Interval.of(Duration.ofDays(-15)
        .minusMinutes(12)
        .minusSeconds(3)));

    assertEquals(
      Interval.of(1, 3, 14, 3, 55, 5, 463123),
      Interval.of(Duration.ofDays(12 * 30 + 3 * 30 + 14)
        .plusHours(3)
        .plusMinutes(55)
        .plusSeconds(5)
        .plusNanos(463123 * 1000)));
  }

  @Test
  public void testToDuration() {
    assertEquals(Duration.ZERO, Interval.of().toDuration());

    assertEquals(
      Duration.ZERO.minus(1, MICROS),
      Interval.of(0, 0, 0, 0, 0, 0, -1).toDuration());

    assertEquals(
      Duration.ofDays(-15)
        .minusMinutes(12)
        .minusSeconds(3),
      Interval.of(0, 0, -15, 0, -12, -3).toDuration());

    assertEquals(
      Duration.between(
        LocalDateTime.of(2024, 2, 1, 13, 30),
        LocalDateTime.of(2024, 3, 1, 12, 0)),
      Interval.of(0, 0, 29, -1, -30, 0, 0).toDuration());
  }
}

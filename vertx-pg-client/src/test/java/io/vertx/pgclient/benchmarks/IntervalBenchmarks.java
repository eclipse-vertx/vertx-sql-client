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

package io.vertx.pgclient.benchmarks;

import io.vertx.pgclient.data.Interval;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.time.Duration;
import java.time.Period;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.MICROS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = {"-Xms8g", "-Xmx8g", "-Xmn7g"})
public class IntervalBenchmarks {

  private Interval interval;

  @Setup
  public void setup() throws IOException, InterruptedException {
    interval = new Interval(-2, 3, 15, -13, 2, -57, -426994);
  }

  @Benchmark
  public void encodeWithDurationAndPeriod(Blackhole blackhole) {
    Duration duration = Duration
      .ofHours(interval.getHours())
      .plusMinutes(interval.getMinutes())
      .plusSeconds(interval.getSeconds())
      .plus(interval.getMicroseconds(), MICROS);
    // days won't be changed
    Period monthYear = Period.of(interval.getYears(), interval.getMonths(), interval.getDays()).normalized();

    blackhole.consume(NANOSECONDS.toMicros(duration.toNanos()));
    blackhole.consume(monthYear.getDays());
    blackhole.consume((int) monthYear.toTotalMonths());
  }

  @Benchmark
  public void encodeWithParts(Blackhole blackhole) {
    // We decompose the interval in 3 parts: months, seconds and micros
    int monthsPart = Math.addExact(Math.multiplyExact(interval.getYears(), 12), interval.getMonths());
    // A long is big enough to store the maximum/minimum value of the seconds part
    long secondsPart = interval.getDays() * 24 * 3600L
                       + interval.getHours() * 3600L
                       + interval.getMinutes() * 60L
                       + interval.getSeconds()
                       + interval.getMicroseconds() / 1000000;
    int microsPart = interval.getMicroseconds() % 1000000;

    // The actual number of months is the sum of the months part and the number of months present in the seconds part
    int months = Math.addExact(monthsPart, Math.toIntExact(secondsPart / 2592000));
    // The actual number of days is computed from the remainder of the previous division
    // It's necessarily smaller than or equal to 29
    int days = (int) secondsPart % 2592000 / 86400;
    // The actual number of micros is the sum of the micros part and the remainder of previous divisions
    // The remainder of previous divisions is necessarily smaller than or equal to a day less a second
    // The microseconds part is smaller than a second
    // Therefore, their sum is necessarily smaller than a day
    long micros = microsPart + secondsPart % 2592000 % 86400 * 1000000;

    blackhole.consume(micros);
    blackhole.consume(days);
    blackhole.consume(months);
  }
}

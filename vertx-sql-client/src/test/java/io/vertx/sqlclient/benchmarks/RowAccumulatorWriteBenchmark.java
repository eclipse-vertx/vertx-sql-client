/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.benchmarks;

import io.vertx.sqlclient.impl.accumulator.RowAccumulator;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static io.vertx.sqlclient.benchmarks.Utils.generateStrings;

@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = {"-Xms8g", "-Xmx8g", "-Xmn7g"})
public class RowAccumulatorWriteBenchmark {

  @Param({"ARRAY_LIST", "CHUNKED_FIXED_SIZE", "CHUNKED_GROWING_SIZE"})
  public RowAccumulatorType rowAccumulatorType;

  @Param({"5", "20", "65", "605", "1820", "5465", "16400"})
  int size;

  @Param({"false", "true"})
  boolean shuffle;

  @Param({"false", "true"})
  boolean gc;

  String[] arr;

  @Setup
  public void setup() throws IOException, InterruptedException {
    arr = generateStrings(size, shuffle, gc);
  }

  @Benchmark
  public RowAccumulator<String> accumulate() {
    RowAccumulator<String> rowAccumulator = rowAccumulatorType.newInstance();
    for (String s : arr) {
      rowAccumulator.accept(s);
    }
    return rowAccumulator;
  }
}

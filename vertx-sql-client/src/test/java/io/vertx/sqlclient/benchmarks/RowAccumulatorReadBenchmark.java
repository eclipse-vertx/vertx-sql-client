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

import io.vertx.sqlclient.impl.accumulator.ArrayListRowAccumulator;
import io.vertx.sqlclient.impl.accumulator.ChunkedRowAccumulator;
import io.vertx.sqlclient.impl.accumulator.RowAccumulator;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;

import static io.vertx.sqlclient.benchmarks.Utils.generateStrings;

@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = {"-Xms8g", "-Xmx8g", "-Xmn7g"})
public class RowAccumulatorReadBenchmark {

  @Param({"5", "20", "65", "605", "1820", "5465"})
  int size;

  @Param({"false", "true"})
  boolean shuffle;

  @Param({"false", "true"})
  boolean gc;

  String[] arr;
  ArrayListRowAccumulator<String> arrayListRowAccumulator;
  ChunkedRowAccumulator<String> chunkedRowAccumulator;

  @Setup
  public void setup() throws IOException, InterruptedException {
    arr = generateStrings(size, shuffle);
    arrayListRowAccumulator = new ArrayListRowAccumulator<>();
    chunkedRowAccumulator = new ChunkedRowAccumulator<>(IntUnaryOperator.identity());
    for (String s : arr) {
      arrayListRowAccumulator.accept(s);
      chunkedRowAccumulator.accept(s);
    }
    if (gc) {
      for (int c = 0; c < 5; c++) {
        System.gc();
        TimeUnit.SECONDS.sleep(1);
      }
    }
  }

  @Benchmark
  public int iterateArrayList() {
    return test(arrayListRowAccumulator);
  }

  @Benchmark
  public int iterateChunked() {
    return test(chunkedRowAccumulator);
  }

  private static int test(RowAccumulator<String> rowAccumulator) {
    int dummy = 0;
    for (String s : rowAccumulator) {
      dummy += s.length();
    }
    return dummy;
  }
}

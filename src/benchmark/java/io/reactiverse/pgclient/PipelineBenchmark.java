/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.reactiverse.pgclient;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class PipelineBenchmark extends PgBenchmarkBase {

  public static final int ITER = 25;

  Tuple args;

  @Override
  public void setup() throws Exception {
    super.setup();
    args = Tuple.of(1);
  }

  @Benchmark
  public void test1(Blackhole blackhole) throws Exception {
    CompletableFuture<PgResult> latch = new CompletableFuture<>();
    AtomicInteger count = new AtomicInteger();
    for (int i = 0;i < ITER;i++) {
      pool.query("SELECT id, randomnumber from WORLD where id=1", ar -> {
        if (ar.succeeded()) {
          if (count.incrementAndGet() == ITER) {
            latch.complete(ar.result());
          }
        } else {
          latch.completeExceptionally(ar.cause());
        }
      });
    }
    blackhole.consume(latch.get());
  }

  @Benchmark
  public void test2(Blackhole blackhole) throws Exception {
    CompletableFuture<PgResult> latch = new CompletableFuture<>();
    doSingle(0, latch);
    blackhole.consume(latch.get());
  }

  private void doSingle(int count, CompletableFuture<PgResult> latch) {
    pool.query("SELECT id, randomnumber from WORLD where id=1", ar -> {
      if (ar.succeeded()) {
        if (count + 1 == ITER) {
          latch.complete(ar.result());
        } else {
          doSingle(count + 1, latch);
        }
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
  }

}

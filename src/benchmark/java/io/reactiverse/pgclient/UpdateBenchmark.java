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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Threads(8)
public class UpdateBenchmark extends PgBenchmarkBase {

  List<Tuple> batch;

  @Override
  public void setup() throws Exception {
    super.setup();
    int len = 10;
    batch = new ArrayList<>();
    Random random = new Random();
    for (int id = 0;id < len;id++) {
      batch.add(Tuple.of(1 + random.nextInt(10000), id));
    }
  }

  @Benchmark
  public void poolPreparedBatchUpdate(Blackhole blackhole) throws Exception {
    CompletableFuture<PgResult<PgRowSet>> latch = new CompletableFuture<>();
    pool.preparedBatch("UPDATE world SET randomnumber=$1 WHERE id=$2", batch, ar -> {
      if (ar.succeeded()) {
        latch.complete(ar.result());
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    blackhole.consume(latch.get());
  }
}

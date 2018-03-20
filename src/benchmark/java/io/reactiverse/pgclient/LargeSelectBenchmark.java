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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.CompletableFuture;

@State(Scope.Benchmark)
@Threads(8)
public class LargeSelectBenchmark extends PgBenchmarkBase {

  @Benchmark
  public void poolPreparedQuery(Blackhole blackhole) throws Exception {
    CompletableFuture<PgResult> latch = new CompletableFuture<>();
    pool.preparedQuery("SELECT id, randomnumber from WORLD", ar -> {
      if (ar.succeeded()) {
        latch.complete(ar.result());
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    blackhole.consume(latch.get());
  }

  @Benchmark
  public void pooledConnectionPreparedQuery(Blackhole blackhole) throws Exception {
    CompletableFuture<PgResult> latch = new CompletableFuture<>();
    pool.getConnection(ar1 -> {
      if (ar1.succeeded()) {
        PgConnection conn = ar1.result();
        conn.preparedQuery("SELECT id, randomnumber from WORLD", ar2 -> {
          conn.close();
          if (ar2.succeeded()) {
            latch.complete(ar2.result());
          } else {
            latch.completeExceptionally(ar2.cause());
          }
        });
      } else {
        latch.completeExceptionally(ar1.cause());
      }
    });
    blackhole.consume(latch.get());
  }

  @Benchmark
  public void pooledConnectionPreparedStatementQuery(Blackhole blackhole) throws Exception {
    CompletableFuture<PgResult> latch = new CompletableFuture<>();
    pool.getConnection(ar1 -> {
      if (ar1.succeeded()) {
        PgConnection conn = ar1.result();
        conn.prepare("SELECT id, randomnumber from WORLD", ar2 -> {
          if (ar2.succeeded()) {
            PgPreparedQuery ps = ar2.result();
            ps.execute(ar3 -> {
              conn.close();
              if (ar3.succeeded()) {
                latch.complete(ar3.result());
              } else {
                latch.completeExceptionally(ar3.cause());
              }
            });
          } else {
            latch.completeExceptionally(ar2.cause());
          }
        });
      } else {
        latch.completeExceptionally(ar1.cause());
      }
    });
    blackhole.consume(latch.get());
  }
}

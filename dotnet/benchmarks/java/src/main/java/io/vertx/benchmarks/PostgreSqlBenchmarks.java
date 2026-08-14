/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.benchmarks;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Tuple;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 2)
@Fork(2)
public class PostgreSqlBenchmarks {

  private Vertx vertx;
  private PgConnection connection;

  @Setup(Level.Trial)
  public void setup() {
    vertx = Vertx.vertx();
    PgConnectOptions options = new PgConnectOptions()
      .setHost(environment("APEX_PG_HOST", "localhost"))
      .setPort(Integer.parseInt(environment("APEX_PG_PORT", "5432")))
      .setDatabase(environment("APEX_PG_DATABASE", "db"))
      .setUser(environment("APEX_PG_USERNAME", "user"))
      .setPassword(environment("APEX_PG_PASSWORD", "pass"));
    connection = PgConnection.connect(vertx, options)
      .toCompletionStage()
      .toCompletableFuture()
      .join();
  }

  @TearDown(Level.Trial)
  public void teardown() {
    connection.close().toCompletionStage().toCompletableFuture().join();
    vertx.close().toCompletionStage().toCompletableFuture().join();
  }

  @Benchmark
  public int simpleQuery() {
    return connection.query("SELECT 1")
      .execute()
      .toCompletionStage()
      .toCompletableFuture()
      .join()
      .iterator()
      .next()
      .getInteger(0);
  }

  @Benchmark
  public int preparedQuery() {
    return connection.preparedQuery("SELECT $1::int4")
      .execute(Tuple.of(42))
      .toCompletionStage()
      .toCompletableFuture()
      .join()
      .iterator()
      .next()
      .getInteger(0);
  }

  private static String environment(String name, String fallback) {
    String value = System.getenv(name);
    return value == null || value.isBlank() ? fallback : value;
  }
}

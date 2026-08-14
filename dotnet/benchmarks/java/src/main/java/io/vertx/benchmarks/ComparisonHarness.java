/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.benchmarks;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

public final class ComparisonHarness {

  public static void main(String[] args) throws Exception {
    int concurrency = Integer.parseInt(environment("APEX_BENCH_CONCURRENCY", "16"));
    double warmupSeconds = Double.parseDouble(environment("APEX_BENCH_WARMUP_SECONDS", "2"));
    double durationSeconds = Double.parseDouble(environment("APEX_BENCH_DURATION_SECONDS", "10"));
    Vertx vertx = Vertx.vertx();
    List<PgConnection> connections = new ArrayList<>(concurrency);
    PgConnectOptions options = new PgConnectOptions()
      .setHost(environment("APEX_PG_HOST", "localhost"))
      .setPort(Integer.parseInt(environment("APEX_PG_PORT", "5432")))
      .setDatabase(environment("APEX_PG_DATABASE", "db"))
      .setUser(environment("APEX_PG_USERNAME", "user"))
      .setPassword(environment("APEX_PG_PASSWORD", "pass"));
    try {
      for (int i = 0; i < concurrency; i++) {
        connections.add(PgConnection.connect(vertx, options)
          .toCompletionStage()
          .toCompletableFuture()
          .join());
      }

      run(connections, warmupSeconds, false);
      System.gc();
      long[] collectionsBefore = collections();
      Result result = run(connections, durationSeconds, true);
      long[] collectionsAfter = collections();
      result.gen0Collections = collectionsAfter[0] - collectionsBefore[0];
      result.gen1Collections = collectionsAfter[1] - collectionsBefore[1];
      System.out.println(result.toJson());
    } finally {
      for (PgConnection connection : connections) {
        connection.close().toCompletionStage().toCompletableFuture().join();
      }
      vertx.close().toCompletionStage().toCompletableFuture().join();
    }
  }

  private static Result run(
      List<PgConnection> connections,
      double durationSeconds,
      boolean record) throws Exception {
    long deadline = System.nanoTime() + (long) (durationSeconds * 1_000_000_000L);
    LongAdder operations = new LongAdder();
    List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
    ExecutorService workers = Executors.newFixedThreadPool(connections.size());
    try {
      List<Future<?>> pending = new ArrayList<>(connections.size());
      for (PgConnection connection : connections) {
        pending.add(workers.submit(() -> {
          while (System.nanoTime() < deadline) {
            long started = System.nanoTime();
            connection.query("SELECT 1")
              .execute()
              .toCompletionStage()
              .toCompletableFuture()
              .join();
            if (record) {
              latencies.add(System.nanoTime() - started);
            }
            operations.increment();
          }
        }));
      }
      for (Future<?> worker : pending) {
        worker.get();
      }
    } finally {
      workers.shutdownNow();
    }

    Collections.sort(latencies);
    long count = operations.sum();
    return new Result(
      connections.size(),
      count,
      durationSeconds,
      count / durationSeconds,
      percentile(latencies, 0.50),
      percentile(latencies, 0.95),
      percentile(latencies, 0.99));
  }

  private static double percentile(List<Long> ordered, double percentile) {
    if (ordered.isEmpty()) {
      return 0;
    }
    int index = Math.max(
      0,
      Math.min(ordered.size() - 1, (int) Math.ceil(percentile * ordered.size()) - 1));
    return ordered.get(index) / 1_000_000d;
  }

  private static long[] collections() {
    long young = 0;
    long old = 0;
    for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
      long count = Math.max(0, bean.getCollectionCount());
      if (bean.getName().toLowerCase(Locale.ROOT).contains("young")) {
        young += count;
      } else {
        old += count;
      }
    }
    return new long[] { young, old };
  }

  private static String environment(String name, String fallback) {
    String value = System.getenv(name);
    return value == null || value.isBlank() ? fallback : value;
  }

  private static final class Result {
    private final int concurrency;
    private final long operations;
    private final double durationSeconds;
    private final double operationsPerSecond;
    private final double p50Milliseconds;
    private final double p95Milliseconds;
    private final double p99Milliseconds;
    private long gen0Collections;
    private long gen1Collections;

    private Result(
        int concurrency,
        long operations,
        double durationSeconds,
        double operationsPerSecond,
        double p50Milliseconds,
        double p95Milliseconds,
        double p99Milliseconds) {
      this.concurrency = concurrency;
      this.operations = operations;
      this.durationSeconds = durationSeconds;
      this.operationsPerSecond = operationsPerSecond;
      this.p50Milliseconds = p50Milliseconds;
      this.p95Milliseconds = p95Milliseconds;
      this.p99Milliseconds = p99Milliseconds;
    }

    private String toJson() {
      return String.format(
        Locale.ROOT,
        """
        {
          "Driver": "vertx",
          "Concurrency": %d,
          "Operations": %d,
          "DurationSeconds": %.6f,
          "OperationsPerSecond": %.6f,
          "P50Milliseconds": %.6f,
          "P95Milliseconds": %.6f,
          "P99Milliseconds": %.6f,
          "AllocatedBytes": -1,
          "Gen0Collections": %d,
          "Gen1Collections": %d,
          "Runtime": "%s",
          "OperatingSystem": "%s",
          "Architecture": "%s"
        }
        """,
        concurrency,
        operations,
        durationSeconds,
        operationsPerSecond,
        p50Milliseconds,
        p95Milliseconds,
        p99Milliseconds,
        gen0Collections,
        gen1Collections,
        System.getProperty("java.version"),
        System.getProperty("os.name") + " " + System.getProperty("os.version"),
        System.getProperty("os.arch"));
    }
  }
}

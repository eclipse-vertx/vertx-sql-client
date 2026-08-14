# Benchmark methodology

## Native microbenchmarks

- `Apex.DriverBenchmarks` uses BenchmarkDotNet for .NET codec allocations and Apex/Npgsql query, prepared-query, and streaming workloads.
- `dotnet/benchmarks/java` uses JMH for equivalent Vert.x PostgreSQL query workloads.
- Native BenchmarkDotNet and JMH scores are reported separately because their harnesses, runtimes, warmup models, and profilers differ.

## Common process harness

`Apex.ComparisonHarness` and `io.vertx.benchmarks.ComparisonHarness` execute the same `SELECT 1` workload with the same database, concurrency, warmup, and measurement duration. Both emit JSON containing operations/second, p50/p95/p99 latency, runtime, OS, architecture, and GC counts. The .NET harness also reports process allocation bytes.

Environment variables:

| Variable | Purpose | Default |
|---|---|---|
| `APEX_PG_CONNECTION_STRING` | .NET PostgreSQL connection string | Required |
| `APEX_PG_HOST`, `APEX_PG_PORT`, `APEX_PG_DATABASE`, `APEX_PG_USERNAME`, `APEX_PG_PASSWORD` | Vert.x connection fields | Local Vert.x test defaults |
| `APEX_BENCH_CONCURRENCY` | Concurrent workers/connections | `16` |
| `APEX_BENCH_WARMUP_SECONDS` | Warmup duration | `2` |
| `APEX_BENCH_DURATION_SECONDS` | Measurement duration | `10` |

Results must record the exact driver commit/package, SDK/JDK, CPU, OS, database image/version, container limits, and harness settings. Short local runs are diagnostic only and are not release claims.

## Initial diagnostic baseline

An August 2026 macOS Arm64 run against the same PostgreSQL 16 container, four workers, one-second warmup, and two-second measurement produced approximately:

| Driver | operations/s | p50 | p95 | p99 | allocated bytes |
|---|---:|---:|---:|---:|---:|
| Apex | 7,506 | 0.444 ms | 1.286 ms | 1.628 ms | 53.9 MB |
| Npgsql | 7,566 | 0.441 ms | 1.290 ms | 1.586 ms | 20.4 MB |
| Vert.x | 7,144 | 0.424 ms | 1.387 ms | 1.773 ms | Not available |

Throughput and latency were similar in this short run, while Apex allocated substantially more. Allocation reduction is therefore the first optimization target. Full artifacts are stored outside the repository in the session benchmark artifacts.

After pooling wire payloads and scheduler `IValueTaskSource<T>` commands and encoding simple queries directly into the pipe, a comparable short Apex run allocated about 42.5 MB across 15,146 operations (approximately 2.8 KB/operation), down from roughly 3.6 KB/operation. The retained baseline artifacts are the source of truth; these short runs remain diagnostic rather than release claims.

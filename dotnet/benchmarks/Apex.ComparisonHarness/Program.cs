/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Collections.Concurrent;
using System.Diagnostics;
using System.Text.Json;
using Apex.PgClient;
using Npgsql;

string driver = args.ElementAtOrDefault(0) ?? "apex";
int concurrency = int.Parse(
  Environment.GetEnvironmentVariable("APEX_BENCH_CONCURRENCY") ?? "16");
TimeSpan warmup = TimeSpan.FromSeconds(double.Parse(
  Environment.GetEnvironmentVariable("APEX_BENCH_WARMUP_SECONDS") ?? "2"));
TimeSpan duration = TimeSpan.FromSeconds(double.Parse(
  Environment.GetEnvironmentVariable("APEX_BENCH_DURATION_SECONDS") ?? "10"));
string connectionString =
  Environment.GetEnvironmentVariable("APEX_PG_CONNECTION_STRING") ??
  throw new InvalidOperationException("Set APEX_PG_CONNECTION_STRING.");

IQueryRunner[] runners = await Task.WhenAll(
  Enumerable.Range(0, concurrency)
    .Select(_ => CreateRunnerAsync(driver, connectionString).AsTask()));
try
{
  await RunPhaseAsync(driver, runners, warmup, record: false);
  GC.Collect();
  GC.WaitForPendingFinalizers();
  GC.Collect();
  long allocatedBefore = GC.GetTotalAllocatedBytes(precise: true);
  int[] collectionsBefore = [GC.CollectionCount(0), GC.CollectionCount(1), GC.CollectionCount(2)];
  HarnessResult result = await RunPhaseAsync(
    driver,
    runners,
    duration,
    record: true);
  result = result with
  {
    AllocatedBytes = GC.GetTotalAllocatedBytes(precise: true) - allocatedBefore,
    Gen0Collections = GC.CollectionCount(0) - collectionsBefore[0],
    Gen1Collections = GC.CollectionCount(1) - collectionsBefore[1],
    Gen2Collections = GC.CollectionCount(2) - collectionsBefore[2],
  };
  Console.WriteLine(JsonSerializer.Serialize(result, new JsonSerializerOptions { WriteIndented = true }));
}
finally
{
  foreach (IQueryRunner runner in runners)
  {
    await runner.DisposeAsync();
  }
}

static async Task<HarnessResult> RunPhaseAsync(
  string driver,
  IQueryRunner[] runners,
  TimeSpan duration,
  bool record)
{
  ConcurrentBag<long> latencies = [];
  long operations = 0;
  using CancellationTokenSource stop = new(duration);
  Task[] workers = runners
    .Select(runner => RunWorkerAsync(
      runner,
      stop.Token,
      record ? latencies : null,
      () => Interlocked.Increment(ref operations)))
    .ToArray();
  Stopwatch elapsed = Stopwatch.StartNew();
  await Task.WhenAll(workers);
  elapsed.Stop();
  long[] ordered = latencies.Order().ToArray();
  return new HarnessResult(
    driver,
    runners.Length,
    operations,
    elapsed.Elapsed.TotalSeconds,
    operations / elapsed.Elapsed.TotalSeconds,
    Percentile(ordered, 0.50),
    Percentile(ordered, 0.95),
    Percentile(ordered, 0.99),
    0,
    0,
    0,
    0,
    Environment.Version.ToString(),
    System.Runtime.InteropServices.RuntimeInformation.OSDescription,
    System.Runtime.InteropServices.RuntimeInformation.ProcessArchitecture.ToString());
}

static async Task RunWorkerAsync(
  IQueryRunner runner,
  CancellationToken cancellationToken,
  ConcurrentBag<long>? latencies,
  Action completed)
{
  while (!cancellationToken.IsCancellationRequested)
  {
    long started = Stopwatch.GetTimestamp();
    try
    {
      await runner.QueryAsync(cancellationToken);
    }

    catch (OperationCanceledException) when (cancellationToken.IsCancellationRequested)
    {
      break;
    }

    latencies?.Add(Stopwatch.GetTimestamp() - started);
    completed();
  }
}

static ValueTask<IQueryRunner> CreateRunnerAsync(string driver, string connectionString) =>
  driver.ToLowerInvariant() switch
  {
    "apex" => WrapAsync(ApexQueryRunner.CreateAsync(connectionString)),
    "npgsql" => WrapAsync(NpgsqlQueryRunner.CreateAsync(connectionString)),
    _ => throw new ArgumentException($"Unknown driver '{driver}'."),
  };

static async ValueTask<IQueryRunner> WrapAsync<T>(ValueTask<T> runner)
  where T : IQueryRunner =>
  await runner;

static double Percentile(long[] ordered, double percentile)
{
  if (ordered.Length == 0)
  {
    return 0;
  }

  int index = Math.Clamp(
    (int)Math.Ceiling(percentile * ordered.Length) - 1,
    0,
    ordered.Length - 1);
  return ordered[index] * 1000d / Stopwatch.Frequency;
}

internal interface IQueryRunner : IAsyncDisposable
{
  ValueTask QueryAsync(CancellationToken cancellationToken);
}

internal sealed class ApexQueryRunner(PgConnection connection) : IQueryRunner
{
  public static async ValueTask<ApexQueryRunner> CreateAsync(string connectionString)
  {
    NpgsqlConnectionStringBuilder builder = new(connectionString);
    string username = builder.Username ??
      throw new InvalidOperationException("Connection string requires Username.");
    PgConnection connection = await PgClient.ConnectAsync(new PgConnectOptions
    {
      Host = builder.Host ??
        throw new InvalidOperationException("Connection string requires Host."),
      Port = builder.Port,
      Database = builder.Database ?? username,
      Username = username,
      Password = builder.Password ?? string.Empty,
      PipeliningLimit = 256,
    });
    return new ApexQueryRunner(connection);
  }

  public async ValueTask QueryAsync(CancellationToken cancellationToken)
  {
    _ = await connection.QueryAsync("SELECT 1", cancellationToken);
  }

  public ValueTask DisposeAsync() => connection.DisposeAsync();
}

internal sealed class NpgsqlQueryRunner(NpgsqlConnection connection) : IQueryRunner
{
  public static async ValueTask<NpgsqlQueryRunner> CreateAsync(string connectionString)
  {
    NpgsqlConnection connection = new(connectionString);
    await connection.OpenAsync();
    return new NpgsqlQueryRunner(connection);
  }

  public async ValueTask QueryAsync(CancellationToken cancellationToken)
  {
    await using NpgsqlCommand command = new("SELECT 1", connection);
    _ = await command.ExecuteScalarAsync(cancellationToken);
  }

  public ValueTask DisposeAsync() => connection.DisposeAsync();
}

internal sealed record HarnessResult(
  string Driver,
  int Concurrency,
  long Operations,
  double DurationSeconds,
  double OperationsPerSecond,
  double P50Milliseconds,
  double P95Milliseconds,
  double P99Milliseconds,
  long AllocatedBytes,
  int Gen0Collections,
  int Gen1Collections,
  int Gen2Collections,
  string Runtime,
  string OperatingSystem,
  string Architecture);

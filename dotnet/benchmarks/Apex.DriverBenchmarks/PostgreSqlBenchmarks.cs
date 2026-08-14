/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.PgClient;
using Apex.SqlClient;
using BenchmarkDotNet.Attributes;
using Npgsql;

namespace Apex.DriverBenchmarks;

[MemoryDiagnoser]
public class PostgreSqlBenchmarks
{
  private PgConnection _apex = null!;
  private ISqlPreparedStatement _apexPrepared = null!;
  private NpgsqlConnection _npgsql = null!;
  private NpgsqlCommand _npgsqlPrepared = null!;

  [GlobalSetup]
  public async Task SetupAsync()
  {
    string connectionString =
        Environment.GetEnvironmentVariable("APEX_PG_CONNECTION_STRING") ??
        throw new InvalidOperationException(
            "Set APEX_PG_CONNECTION_STRING before running database benchmarks.");
    NpgsqlConnectionStringBuilder builder = new(connectionString);
    _npgsql = new NpgsqlConnection(builder.ConnectionString);
    await _npgsql.OpenAsync();
    string username = builder.Username ??
        throw new InvalidOperationException("The benchmark connection string requires Username.");
    _apex = await Apex.PgClient.PgClient.ConnectAsync(new PgConnectOptions
    {
      Host = builder.Host ??
            throw new InvalidOperationException("The benchmark connection string requires Host."),
      Port = builder.Port,
      Database = builder.Database ?? username,
      Username = username,
      Password = builder.Password ?? string.Empty,
      SslMode = builder.SslMode == SslMode.Disable ? PgSslMode.Disable : PgSslMode.Prefer,
    });
    _apexPrepared = await _apex.PrepareAsync("SELECT $1::int4");
    _npgsqlPrepared = new NpgsqlCommand("SELECT $1::int4", _npgsql);
    _npgsqlPrepared.Parameters.Add(new NpgsqlParameter<int> { TypedValue = 42 });
    await _npgsqlPrepared.PrepareAsync();
  }

  [GlobalCleanup]
  public async Task CleanupAsync()
  {
    await _apexPrepared.DisposeAsync();
    await _npgsqlPrepared.DisposeAsync();
    await _apex.DisposeAsync();
    await _npgsql.DisposeAsync();
  }

  [Benchmark(Baseline = true)]
  public async Task<int> NpgsqlSimpleQueryAsync()
  {
    await using NpgsqlCommand command = new("SELECT 1", _npgsql);
    return Convert.ToInt32(await command.ExecuteScalarAsync());
  }

  [Benchmark]
  public async Task<int> ApexSimpleQueryAsync()
  {
    SqlRowSet rows = await _apex.QueryAsync("SELECT 1");
    return rows[0].Get<int>(0);
  }

  [Benchmark]
  public async Task<int> NpgsqlPreparedQueryAsync() =>
    Convert.ToInt32(await _npgsqlPrepared.ExecuteScalarAsync());

  [Benchmark]
  public async Task<int> ApexPreparedQueryAsync()
  {
    SqlRowSet rows = await _apexPrepared.QueryAsync(SqlParameters.Create(42));
    return rows[0].Get<int>(0);
  }

  [Benchmark]
  public async Task<int> NpgsqlStream100RowsAsync()
  {
    await using NpgsqlCommand command =
      new("SELECT generate_series(1, 100)::int4", _npgsql);
    await using NpgsqlDataReader reader = await command.ExecuteReaderAsync();
    int sum = 0;
    while (await reader.ReadAsync())
    {
      sum += reader.GetInt32(0);
    }

    return sum;
  }

  [Benchmark]
  public async Task<int> ApexStream100RowsAsync()
  {
    int sum = 0;
    await foreach (SqlRow row in _apex.StreamAsync(
                     "SELECT generate_series(1, 100)::int4",
                     fetchSize: 16))
    {
      sum += row.Get<int>(0);
    }

    return sum;
  }
}

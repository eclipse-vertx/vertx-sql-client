/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;
using Testcontainers.PostgreSql;

namespace Apex.PgClient.IntegrationTests;

[TestClass]
public sealed class PgConnectionIntegrationTests
{
  private PostgreSqlContainer? _container;

  [TestInitialize]
  public async Task StartPostgreSqlAsync()
  {
    string image = Environment.GetEnvironmentVariable("POSTGRES_IMAGE") ?? "postgres:16-alpine";
    _container = new PostgreSqlBuilder(image)
        .WithDatabase("db")
        .WithUsername("user")
        .WithPassword("pass")
        .Build();
    await _container.StartAsync();
  }

  [TestCleanup]
  public async Task StopPostgreSqlAsync()
  {
    if (_container is not null)
    {
      await _container.DisposeAsync();
    }
  }

  [TestMethod]
  public async Task ConnectsQueriesAndRollsBack()
  {
    PostgreSqlContainer container = _container ??
        throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    SqlRowSet scalar = await connection.QueryAsync("SELECT 1 AS id, 'hello' AS message");

    Assert.AreEqual(1, scalar.Count);
    Assert.AreEqual(1, scalar[0].Get<int>(0));
    Assert.AreEqual("hello", scalar[0].Get<string>("message"));
    Assert.AreEqual("PostgreSQL", connection.DatabaseMetadata.ProductName);
    Assert.IsGreaterThanOrEqualTo(14, connection.DatabaseMetadata.MajorVersion);

    SqlRowSet parameterized = await connection.QueryAsync(
        "SELECT $1::int4 AS id, $2::text AS message",
        SqlParameters.Create(42, "forty-two"));
    Assert.AreEqual(42, parameterized[0].Get<int>("id"));
    Assert.AreEqual("forty-two", parameterized[0].Get<string>("message"));

    await connection.ExecuteAsync("CREATE TEMP TABLE values_to_rollback (value int)");
    await using (ISqlTransaction transaction = await connection.BeginTransactionAsync())
    {
      await connection.ExecuteAsync("INSERT INTO values_to_rollback VALUES (1)");
    }

    SqlRowSet count = await connection.QueryAsync(
        "SELECT COUNT(*)::int8 AS count FROM values_to_rollback");
    Assert.AreEqual(0L, count[0].Get<long>("count"));
  }

  [TestMethod]
  public async Task SurfacesPostgreSqlErrorFields()
  {
    PostgreSqlContainer container = _container ??
        throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    PgException exception = await Assert.ThrowsExactlyAsync<PgException>(
        () => connection.QueryAsync("SELECT missing_column").AsTask());

    Assert.AreEqual("42703", exception.SqlState);
    Assert.IsNotNull(exception.Severity);
  }

  [TestMethod]
  public async Task FetchesServerCursorAndStreamsWithBackpressure()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    await using (ISqlTransaction transaction = await connection.BeginTransactionAsync())
    await using (ISqlPreparedStatement statement =
                 await connection.PrepareAsync("SELECT generate_series(1, 5)::int4 AS value"))
    await using (ISqlCursor cursor = await statement.OpenCursorAsync(fetchSize: 2))
    {
      SqlRowSet first = await cursor.ReadAsync(2);
      SqlRowSet second = await cursor.ReadAsync(2);
      SqlRowSet third = await cursor.ReadAsync(2);

      CollectionAssert.AreEqual(new[] { 1, 2 }, first.Select(static row => row.Get<int>(0)).ToArray());
      CollectionAssert.AreEqual(new[] { 3, 4 }, second.Select(static row => row.Get<int>(0)).ToArray());
      CollectionAssert.AreEqual(new[] { 5 }, third.Select(static row => row.Get<int>(0)).ToArray());
      Assert.IsFalse(cursor.HasMore);
      await transaction.CommitAsync();
    }

    List<int> streamed = [];
    await foreach (SqlRow row in connection.StreamAsync(
                     "SELECT generate_series(1, 5)::int4 AS value",
                     fetchSize: 2))
    {
      streamed.Add(row.Get<int>(0));
    }

    CollectionAssert.AreEqual(new[] { 1, 2, 3, 4, 5 }, streamed);
  }

  [TestMethod]
  public async Task CancellationLeavesConnectionReusable()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    using CancellationTokenSource cancellation = new(TimeSpan.FromMilliseconds(200));

    await Assert.ThrowsAsync<OperationCanceledException>(
      () => connection.QueryAsync("SELECT pg_sleep(10)", cancellation.Token).AsTask());

    SqlRowSet rows = await connection.QueryAsync("SELECT 1::int4 AS value");
    Assert.AreEqual(1, rows[0].Get<int>(0));
  }

  [TestMethod]
  public async Task DecodesTextAndBinaryTypeMatrix()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
    };

    const string projection =
      """
      true AS boolean_value,
      2::int2 AS int2_value,
      3::int4 AS int4_value,
      4::int8 AS int8_value,
      1.5::float4 AS float4_value,
      2.5::float8 AS float8_value,
      12345678901234567890.1234::numeric AS numeric_value,
      '12345678-1234-5678-9012-123456789abc'::uuid AS uuid_value,
      '2026-08-14'::date AS date_value,
      '12:34:56.123456'::time AS time_value,
      '12:34:56+02'::timetz AS timetz_value,
      '2026-08-14 12:34:56.123456'::timestamp AS timestamp_value,
      '2026-08-14 12:34:56.123456+00'::timestamptz AS timestamptz_value,
      interval '1 year 2 months 3 days 4 hours 5 minutes 6.123456 seconds' AS interval_value,
      decode('0001feff', 'hex') AS bytea_value,
      '{"ok":true}'::jsonb AS json_value,
      point(1.5, -2.25) AS point_value,
      '192.0.2.1/24'::inet AS inet_value,
      '2001:db8::/64'::cidr AS cidr_value,
      12.34::money AS money_value,
      ARRAY[1, NULL, 3]::int4[] AS array_value
      """;

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    SqlRow text = (await connection.QueryAsync("SELECT " + projection))[0];
    AssertTypeValues(text);

    SqlRow binary = (await connection.QueryAsync(
      "SELECT " + projection + ", $1::int4 AS parameter_value",
      SqlParameters.Create(42)))[0];
    AssertTypeValues(binary);
    Assert.AreEqual(42, binary.Get<int>("parameter_value"));
  }

  [TestMethod]
  public async Task RepreparesInvalidatedCachedStatement()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
      CachePreparedStatements = true,
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    const string sql = "SELECT $1::int4 AS value";
    SqlRowSet first = await connection.QueryAsync(sql, SqlParameters.Create(1));
    await connection.ExecuteAsync("DEALLOCATE ALL");
    SqlRowSet second = await connection.QueryAsync(sql, SqlParameters.Create(2));

    Assert.AreEqual(1, first[0].Get<int>(0));
    Assert.AreEqual(2, second[0].Get<int>(0));
  }

  [TestMethod]
  public async Task PipelinesCommandsAndContinuesAfterSqlError()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
      PipeliningLimit = 16,
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    Task<SqlRowSet>[] queries = Enumerable.Range(0, 100)
      .Select(index => connection.QueryAsync(
        "SELECT $1::int4 AS value",
        SqlParameters.Create(index)).AsTask())
      .ToArray();
    SqlRowSet[] results = await Task.WhenAll(queries);

    for (int i = 0; i < results.Length; i++)
    {
      Assert.AreEqual(i, results[i][0].Get<int>(0));
    }

    Task<SqlRowSet> before = connection.QueryAsync("SELECT 1::int4").AsTask();
    Task<SqlRowSet> failure = connection.QueryAsync("SELECT missing_column").AsTask();
    Task<SqlRowSet> after = connection.QueryAsync("SELECT 2::int4").AsTask();
    Assert.AreEqual(1, (await before)[0].Get<int>(0));
    await Assert.ThrowsExactlyAsync<PgException>(() => failure);
    Assert.AreEqual(2, (await after)[0].Get<int>(0));
  }

  [TestMethod]
  public async Task ReceivesNotificationsAndReconnectsSubscriptions()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
    };

    await using PgSubscriber subscriber = await PgClient.SubscribeAsync(
      options,
      static _ => TimeSpan.FromMilliseconds(50));
    await subscriber.SubscribeAsync("apex events");
    await using PgConnection sender = await PgClient.ConnectAsync(options);
    await sender.ExecuteAsync("""NOTIFY "apex events", 'first'""");
    PgNotification first = await NextNotificationAsync(
      subscriber.Notifications,
      TimeSpan.FromSeconds(5));
    Assert.AreEqual("first", first.Payload);

    int firstProcessId = subscriber.ProcessId;
    await sender.QueryAsync(
      "SELECT pg_terminate_backend($1::int4)",
      SqlParameters.Create(firstProcessId));
    using CancellationTokenSource reconnected = new(TimeSpan.FromSeconds(10));
    while (subscriber.ProcessId == firstProcessId)
    {
      await Task.Delay(25, reconnected.Token);
    }

    await sender.ExecuteAsync("""NOTIFY "apex events", 'second'""");
    PgNotification second = await NextNotificationAsync(
      subscriber.Notifications,
      TimeSpan.FromSeconds(5));
    Assert.AreEqual("second", second.Payload);
  }

  [TestMethod]
  public async Task EnforcesLayer7PreparedStatementScope()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
      UseLayer7Proxy = true,
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    await Assert.ThrowsExactlyAsync<InvalidOperationException>(
      () => connection.PrepareAsync("SELECT 1").AsTask());

    await using ISqlTransaction transaction = await connection.BeginTransactionAsync();
    await using ISqlPreparedStatement statement =
      await connection.PrepareAsync("SELECT 1::int4");
    Assert.AreEqual(1, (await statement.QueryAsync())[0].Get<int>(0));
    await statement.DisposeAsync();
    await transaction.CommitAsync();
  }

  [TestMethod]
  public async Task ExecutesPreparedBatchInSubmissionOrder()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
      PipeliningLimit = 8,
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    await connection.ExecuteAsync("CREATE TEMP TABLE batch_values (value int4)");
    await using ISqlPreparedStatement statement =
      await connection.PrepareAsync("INSERT INTO batch_values VALUES ($1::int4)");
    SqlParameters[] batch = Enumerable.Range(0, 20)
      .Select(static value => SqlParameters.Create(value))
      .ToArray();
    IReadOnlyList<SqlCommandResult> results = await statement.ExecuteBatchAsync(batch);

    Assert.AreEqual(20, results.Count);
    Assert.IsTrue(results.All(static result => result.AffectedRows == 1));
    Assert.AreEqual(
      20L,
      (await connection.QueryAsync("SELECT COUNT(*)::int8 FROM batch_values"))[0].Get<long>(0));
  }

  [TestMethod]
  public async Task DecodesCustomEnumAsStringInTextFormat()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    await connection.ExecuteAsync("CREATE TYPE mood AS ENUM ('happy', 'sad')");
    Assert.AreEqual(
      "happy",
      (await connection.QueryAsync("SELECT 'happy'::mood"))[0].Get<string>(0));
  }

  [TestMethod]
  public async Task RejectsNestedTransactionAndHandlesParameterStatus()
  {
    PostgreSqlContainer container = _container ??
      throw new InvalidOperationException("The PostgreSQL container is not running.");
    PgConnectOptions options = new()
    {
      Host = container.Hostname,
      Port = container.GetMappedPublicPort(5432),
      Database = "db",
      Username = "user",
      Password = "pass",
    };

    await using PgConnection connection = await PgClient.ConnectAsync(options);
    await connection.ExecuteAsync("SET application_name = 'apex-runtime-status'");
    await using ISqlTransaction transaction = await connection.BeginTransactionAsync();
    await Assert.ThrowsExactlyAsync<InvalidOperationException>(
      () => connection.BeginTransactionAsync().AsTask());
    await transaction.RollbackAsync();
    Assert.AreEqual(1, (await connection.QueryAsync("SELECT 1::int4"))[0].Get<int>(0));
  }

  private static void AssertTypeValues(SqlRow row)
  {
    Assert.IsTrue(row.Get<bool>("boolean_value"));
    Assert.AreEqual((short)2, row.Get<short>("int2_value"));
    Assert.AreEqual(3, row.Get<int>("int4_value"));
    Assert.AreEqual(4L, row.Get<long>("int8_value"));
    Assert.AreEqual(1.5f, row.Get<float>("float4_value"));
    Assert.AreEqual(2.5d, row.Get<double>("float8_value"));
    Assert.AreEqual(
      "12345678901234567890.1234",
      row.Get<PgNumeric>("numeric_value").ToString());
    Assert.AreEqual(
      Guid.Parse("12345678-1234-5678-9012-123456789abc"),
      row.Get<Guid>("uuid_value"));
    Assert.AreEqual(new DateOnly(2026, 8, 14), row.Get<DateOnly>("date_value"));
    Assert.AreEqual(new TimeOnly(12, 34, 56, 123, 456), row.Get<TimeOnly>("time_value"));
    Assert.AreEqual(TimeSpan.FromHours(2), row.Get<PgTimeWithTimeZone>("timetz_value").Offset);
    Assert.AreEqual(
      new PgInterval(1, 2, 3, 4, 5, 6, 123456),
      row.Get<PgInterval>("interval_value"));
    CollectionAssert.AreEqual(
      new byte[] { 0, 1, 254, 255 },
      row.Get<byte[]>("bytea_value"));
    Assert.IsTrue(row.Get<System.Text.Json.JsonElement>("json_value").GetProperty("ok").GetBoolean());
    Assert.AreEqual(new PgPoint(1.5, -2.25), row.Get<PgPoint>("point_value"));
    Assert.AreEqual(24, row.Get<PgInet>("inet_value").PrefixLength);
    Assert.AreEqual(64, row.Get<PgCidr>("cidr_value").PrefixLength);
    Assert.AreEqual(12.34m, row.Get<PgMoney>("money_value").Value);
    CollectionAssert.AreEqual(
      new object?[] { 1, null, 3 },
      row.Get<object?[]>("array_value"));
  }

  private static async ValueTask<PgNotification> NextNotificationAsync(
    IAsyncEnumerable<PgNotification> notifications,
    TimeSpan timeout)
  {
    using CancellationTokenSource cancellation = new(timeout);
    await foreach (PgNotification notification in notifications.WithCancellation(cancellation.Token))
    {
      return notification;
    }

    throw new InvalidOperationException("The PostgreSQL notification stream completed.");
  }
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;
using Apex.SqlClient.SpecificationTests;
using Testcontainers.PostgreSql;

namespace Apex.PgClient.IntegrationTests;

[TestClass]
public sealed class PgSqlClientSpecificationTests : SqlClientSpecificationTests
{
  private PostgreSqlContainer? _container;

  private PgConnectOptions Options
  {
    get
    {
      PostgreSqlContainer container = _container ??
        throw new InvalidOperationException("The PostgreSQL container is not running.");
      return new PgConnectOptions
      {
        Host = container.Hostname,
        Port = container.GetMappedPublicPort(5432),
        Database = "db",
        Username = "user",
        Password = "pass",
        PipeliningLimit = 8,
      };
    }
  }

  protected override string ParameterizedScalarSql => "SELECT $1::int4";

  protected override string CreateTemporaryTableSql =>
    "CREATE TEMP TABLE specification_values (value int4)";

  protected override string InsertTemporaryValueSql =>
    "INSERT INTO specification_values VALUES ($1::int4)";

  protected override string CountTemporaryValuesSql =>
    "SELECT COUNT(*)::int8 FROM specification_values";

  protected override string SequenceSql =>
    "SELECT generate_series(1, 10)::int4";

  protected override string LongRunningSql => "SELECT pg_sleep(10)";

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

  protected override async ValueTask<ISqlConnection> OpenConnectionAsync(
    CancellationToken cancellationToken = default) =>
    await PgClient.ConnectAsync(Options, cancellationToken);

  protected override ISqlPool CreatePool() =>
    PgPool.Create(Options, new SqlPoolOptions { MaximumSize = 4 });
}

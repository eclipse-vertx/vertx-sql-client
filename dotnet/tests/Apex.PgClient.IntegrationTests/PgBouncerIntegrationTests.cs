/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;
using DotNet.Testcontainers.Builders;
using DotNet.Testcontainers.Containers;
using DotNet.Testcontainers.Networks;
using Testcontainers.PostgreSql;

namespace Apex.PgClient.IntegrationTests;

[TestClass]
[DoNotParallelize]
public sealed class PgBouncerIntegrationTests
{
  [TestMethod]
  public async Task ExecutesThroughTransactionPool()
  {
    INetwork network = new NetworkBuilder().Build();
    PostgreSqlContainer postgres = new PostgreSqlBuilder("postgres:16-alpine")
      .WithDatabase("db")
      .WithUsername("user")
      .WithPassword("pass")
      .WithNetwork(network)
      .WithNetworkAliases("postgres")
      .Build();
    IContainer pgbouncer = new ContainerBuilder("edoburu/pgbouncer:latest")
      .WithEnvironment("DB_HOST", "postgres")
      .WithEnvironment("DB_PORT", "5432")
      .WithEnvironment("DB_USER", "user")
      .WithEnvironment("DB_PASSWORD", "pass")
      .WithEnvironment("DB_NAME", "db")
      .WithEnvironment("POOL_MODE", "transaction")
      .WithEnvironment("AUTH_TYPE", "scram-sha-256")
      .WithNetwork(network)
      .WithPortBinding(5432, true)
      .DependsOn(postgres)
      .WithWaitStrategy(Wait.ForUnixContainer().UntilInternalTcpPortIsAvailable(5432))
      .Build();

    await network.CreateAsync(CancellationToken.None);
    try
    {
      await postgres.StartAsync();
      await pgbouncer.StartAsync();
      PgConnectOptions options = new()
      {
        Host = pgbouncer.Hostname,
        Port = pgbouncer.GetMappedPublicPort(5432),
        Database = "db",
        Username = "user",
        Password = "pass",
        UseLayer7Proxy = true,
      };

      await using PgConnection connection = await PgClient.ConnectAsync(options);
      for (int i = 0; i < 20; i++)
      {
        SqlRowSet rows = await connection.QueryAsync(
          "SELECT $1::int4 AS value",
          SqlParameters.Create(i));
        Assert.AreEqual(i, rows[0].Get<int>(0));
      }

      await using ISqlTransaction transaction = await connection.BeginTransactionAsync();
      await using ISqlPreparedStatement statement =
        await connection.PrepareAsync("SELECT $1::int4 AS value");
      Assert.AreEqual(
        42,
        (await statement.QueryAsync(SqlParameters.Create(42)))[0].Get<int>(0));
      await statement.DisposeAsync();
      await transaction.CommitAsync();
    }
    finally
    {
      await pgbouncer.DisposeAsync();
      await postgres.DisposeAsync();
      await network.DisposeAsync();
    }
  }

  [TestMethod]
  public async Task RejectsPreparedCacheWithLayer7Proxy()
  {
    PgConnectOptions options = new()
    {
      UseLayer7Proxy = true,
      CachePreparedStatements = true,
    };

    await Assert.ThrowsExactlyAsync<ArgumentException>(
      () => PgClient.ConnectAsync(options).AsTask());
  }
}

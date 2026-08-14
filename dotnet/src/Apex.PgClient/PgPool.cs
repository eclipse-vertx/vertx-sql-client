/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Runtime.CompilerServices;
using Apex.SqlClient;
using Apex.SqlClient.Internal;

namespace Apex.PgClient;

public sealed class PgPool : ISqlPool
{
  private readonly SqlConnectionPool<PgConnection> _pool;

  private PgPool(PgConnectOptions connectOptions, SqlPoolOptions poolOptions)
  {
    _pool = new SqlConnectionPool<PgConnection>(
        poolOptions,
        cancellationToken => PgClient.ConnectAsync(connectOptions, cancellationToken),
        static connection => connection.IsReadyForPool);
  }

  public int Size => _pool.Size;

  public static PgPool Create(
      PgConnectOptions connectOptions,
      SqlPoolOptions? poolOptions = null)
  {
    ArgumentNullException.ThrowIfNull(connectOptions);
    return new PgPool(connectOptions, poolOptions ?? new SqlPoolOptions());
  }

  public ValueTask<ISqlConnection> GetConnectionAsync(
      CancellationToken cancellationToken = default) =>
      _pool.GetConnectionAsync(cancellationToken);

  public ValueTask<SqlRowSet> QueryAsync(
      string sql,
      CancellationToken cancellationToken = default) =>
      _pool.QueryAsync(sql, cancellationToken);

  public ValueTask<SqlRowSet> QueryAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default) =>
      _pool.QueryAsync(sql, parameters, cancellationToken);

  public ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      CancellationToken cancellationToken = default) =>
      _pool.ExecuteAsync(sql, cancellationToken);

  public ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default) =>
      _pool.ExecuteAsync(sql, parameters, cancellationToken);

  public async IAsyncEnumerable<SqlRow> StreamAsync(
      string sql,
      SqlParameters parameters = default,
      int fetchSize = 50,
      [EnumeratorCancellation] CancellationToken cancellationToken = default)
  {
    await foreach (SqlRow row in _pool.StreamAsync(
                       sql,
                       parameters,
                       fetchSize,
                       cancellationToken).ConfigureAwait(false))
    {
      yield return row;
    }
  }

  public ValueTask DisposeAsync() => _pool.DisposeAsync();
}

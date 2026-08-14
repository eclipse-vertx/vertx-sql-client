/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;
using System.Runtime.CompilerServices;

namespace Apex.PgClient;

internal sealed class PgPreparedStatement : ISqlPreparedStatement
{
  private readonly PgConnection _connection;
  private readonly string _name;
  private bool _disposed;

  public PgPreparedStatement(PgConnection connection, string name, string sql)
  {
    _connection = connection;
    _name = name;
    Sql = sql;
  }

  public string Sql { get; }

  public ValueTask<SqlRowSet> QueryAsync(
      SqlParameters parameters = default,
      CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    return _connection.ExecutePreparedAsync(_name, parameters, cancellationToken);
  }

  public async ValueTask<SqlCommandResult> ExecuteAsync(
      SqlParameters parameters = default,
      CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    SqlRowSet rows =
        await _connection.ExecutePreparedAsync(_name, parameters, cancellationToken).ConfigureAwait(false);
    return new SqlCommandResult(rows.AffectedRows, rows.CommandTag);
  }

  public async ValueTask<IReadOnlyList<SqlCommandResult>> ExecuteBatchAsync(
      IReadOnlyList<SqlParameters> batch,
      CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    ArgumentNullException.ThrowIfNull(batch);
    Task<SqlCommandResult>[] pending = new Task<SqlCommandResult>[batch.Count];
    for (int i = 0; i < batch.Count; i++)
    {
      pending[i] = ExecuteAsync(batch[i], cancellationToken).AsTask();
    }

    return await Task.WhenAll(pending).ConfigureAwait(false);
  }

  public async ValueTask<ISqlCursor> OpenCursorAsync(
      SqlParameters parameters = default,
      int fetchSize = 50,
      CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    if (fetchSize <= 0)
    {
      throw new ArgumentOutOfRangeException(nameof(fetchSize));
    }

    cancellationToken.ThrowIfCancellationRequested();
    return await _connection.CreateCursorAsync(
      _name,
      parameters,
      fetchSize,
      cancellationToken).ConfigureAwait(false);
  }

  public async IAsyncEnumerable<SqlRow> StreamAsync(
    SqlParameters parameters = default,
    int fetchSize = 50,
    [EnumeratorCancellation] CancellationToken cancellationToken = default)
  {
    ObjectDisposedException.ThrowIf(_disposed, this);
    if (fetchSize <= 0)
    {
      throw new ArgumentOutOfRangeException(nameof(fetchSize));
    }

    await foreach (SqlRow row in _connection.StreamPreparedAsync(
                     _name,
                     parameters,
                     fetchSize,
                     cancellationToken).ConfigureAwait(false))
    {
      yield return row;
    }
  }

  public async ValueTask DisposeAsync()
  {
    if (_disposed)
    {
      return;
    }

    _disposed = true;
    await _connection.ClosePreparedAsync(_name).ConfigureAwait(false);
  }
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

/// <summary>Provides asynchronous SQL operations without depending on ADO.NET.</summary>
public interface ISqlClient : IAsyncDisposable
{
  ValueTask<SqlRowSet> QueryAsync(string sql, CancellationToken cancellationToken = default);

  ValueTask<SqlRowSet> QueryAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default);

  ValueTask<SqlCommandResult> ExecuteAsync(string sql, CancellationToken cancellationToken = default);

  ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default);

  IAsyncEnumerable<SqlRow> StreamAsync(
      string sql,
      SqlParameters parameters = default,
      int fetchSize = 50,
      CancellationToken cancellationToken = default);
}

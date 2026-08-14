/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Runtime.CompilerServices;

namespace Apex.SqlClient;

public static class SqlClientExtensions
{
  public static async ValueTask<IReadOnlyList<T>> QueryMappedAsync<T>(
    this ISqlClient client,
    string sql,
    Func<SqlRow, T> mapper,
    SqlParameters parameters = default,
    CancellationToken cancellationToken = default)
  {
    ArgumentNullException.ThrowIfNull(client);
    ArgumentNullException.ThrowIfNull(mapper);
    SqlRowSet rows = await client.QueryAsync(sql, parameters, cancellationToken).ConfigureAwait(false);
    T[] mapped = new T[rows.Count];
    for (int i = 0; i < rows.Count; i++)
    {
      mapped[i] = mapper(rows[i]);
    }

    return mapped;
  }

  public static async ValueTask<T> QueryCollectedAsync<T>(
    this ISqlClient client,
    string sql,
    Func<IReadOnlyList<SqlRow>, T> collector,
    SqlParameters parameters = default,
    CancellationToken cancellationToken = default)
  {
    ArgumentNullException.ThrowIfNull(client);
    ArgumentNullException.ThrowIfNull(collector);
    SqlRowSet rows = await client.QueryAsync(sql, parameters, cancellationToken).ConfigureAwait(false);
    return collector(rows);
  }

  public static async IAsyncEnumerable<T> StreamMappedAsync<T>(
    this ISqlClient client,
    string sql,
    Func<SqlRow, T> mapper,
    SqlParameters parameters = default,
    int fetchSize = 50,
    [EnumeratorCancellation] CancellationToken cancellationToken = default)
  {
    ArgumentNullException.ThrowIfNull(client);
    ArgumentNullException.ThrowIfNull(mapper);
    await foreach (SqlRow row in client.StreamAsync(
                     sql,
                     parameters,
                     fetchSize,
                     cancellationToken).ConfigureAwait(false))
    {
      yield return mapper(row);
    }
  }
}

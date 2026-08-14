/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

/// <summary>A progressively fetched result set.</summary>
public interface ISqlCursor : IAsyncDisposable
{
  bool HasMore { get; }

  IReadOnlyList<SqlColumn> Columns { get; }

  ValueTask<SqlRowSet> ReadAsync(int count, CancellationToken cancellationToken = default);
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

/// <summary>A physical or leased connection to a database server.</summary>
public interface ISqlConnection : ISqlClient
{
  bool IsSecure { get; }

  DatabaseMetadata DatabaseMetadata { get; }

  ValueTask<ISqlPreparedStatement> PrepareAsync(
      string sql,
      CancellationToken cancellationToken = default);

  ValueTask<ISqlTransaction> BeginTransactionAsync(CancellationToken cancellationToken = default);
}

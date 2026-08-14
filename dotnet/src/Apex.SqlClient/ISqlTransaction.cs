/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

/// <summary>A transaction whose disposal rolls back when it has not committed.</summary>
public interface ISqlTransaction : IAsyncDisposable
{
  bool IsCompleted { get; }

  ValueTask CommitAsync(CancellationToken cancellationToken = default);

  ValueTask RollbackAsync(CancellationToken cancellationToken = default);
}

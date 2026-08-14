/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;

namespace Apex.PgClient;

internal sealed class PgTransaction : ISqlTransaction
{
  private readonly PgConnection _connection;

  public PgTransaction(PgConnection connection)
  {
    _connection = connection;
  }

  public bool IsCompleted { get; private set; }

  public async ValueTask CommitAsync(CancellationToken cancellationToken = default)
  {
    ThrowIfCompleted();
    await _connection.ExecuteTransactionControlAsync("COMMIT", cancellationToken).ConfigureAwait(false);
    IsCompleted = true;
  }

  public async ValueTask RollbackAsync(CancellationToken cancellationToken = default)
  {
    if (IsCompleted)
    {
      return;
    }

    await _connection.ExecuteTransactionControlAsync("ROLLBACK", cancellationToken).ConfigureAwait(false);
    IsCompleted = true;
  }

  public async ValueTask DisposeAsync()
  {
    if (!IsCompleted)
    {
      await RollbackAsync(CancellationToken.None).ConfigureAwait(false);
    }
  }

  private void ThrowIfCompleted()
  {
    if (IsCompleted)
    {
      throw new InvalidOperationException("The transaction has already completed.");
    }
  }
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

/// <summary>A bounded pool of reusable SQL connections.</summary>
public interface ISqlPool : ISqlClient
{
  int Size { get; }

  ValueTask<ISqlConnection> GetConnectionAsync(CancellationToken cancellationToken = default);

  async ValueTask<TResult> WithConnectionAsync<TResult>(
      Func<ISqlConnection, CancellationToken, ValueTask<TResult>> operation,
      CancellationToken cancellationToken = default)
  {
    ArgumentNullException.ThrowIfNull(operation);
    await using ISqlConnection connection = await GetConnectionAsync(cancellationToken).ConfigureAwait(false);
    return await operation(connection, cancellationToken).ConfigureAwait(false);
  }

  async ValueTask<TResult> WithTransactionAsync<TResult>(
      Func<ISqlConnection, CancellationToken, ValueTask<TResult>> operation,
      CancellationToken cancellationToken = default)
  {
    ArgumentNullException.ThrowIfNull(operation);
    await using ISqlConnection connection = await GetConnectionAsync(cancellationToken).ConfigureAwait(false);
    await using ISqlTransaction transaction =
        await connection.BeginTransactionAsync(cancellationToken).ConfigureAwait(false);

    try
    {
      TResult result = await operation(connection, cancellationToken).ConfigureAwait(false);
      await transaction.CommitAsync(cancellationToken).ConfigureAwait(false);
      return result;
    }
    catch (Exception operationError)
    {
      try
      {
        await transaction.RollbackAsync(CancellationToken.None).ConfigureAwait(false);
      }
      catch (Exception rollbackError)
      {
        throw new AggregateException(operationError, rollbackError);
      }

      System.Runtime.ExceptionServices.ExceptionDispatchInfo.Capture(operationError).Throw();
      throw;
    }
  }
}

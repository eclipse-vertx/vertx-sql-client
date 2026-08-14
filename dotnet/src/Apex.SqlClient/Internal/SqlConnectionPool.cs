/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Runtime.CompilerServices;

namespace Apex.SqlClient.Internal;

internal delegate ValueTask<TConnection> SqlConnectionFactory<TConnection>(
    CancellationToken cancellationToken)
    where TConnection : class, ISqlConnection;

internal delegate bool SqlConnectionReusePredicate<in TConnection>(TConnection connection)
    where TConnection : class, ISqlConnection;

internal sealed class SqlConnectionPool<TConnection> : ISqlPool
    where TConnection : class, ISqlConnection
{
  private readonly object _idleGate = new();
  private readonly Stack<PooledConnection> _idle = new();
  private readonly SemaphoreSlim _slots;
  private readonly CancellationTokenSource _disposeCancellation = new();
  private readonly SqlPoolOptions _options;
  private readonly SqlConnectionFactory<TConnection> _connectionFactory;
  private readonly SqlConnectionReusePredicate<TConnection> _reusePredicate;
  private readonly TimeProvider _timeProvider;
  private readonly ITimer? _cleaner;
  private int _size;
  private int _waiters;
  private int _disposed;

  internal SqlConnectionPool(
      SqlPoolOptions options,
      SqlConnectionFactory<TConnection> connectionFactory,
      SqlConnectionReusePredicate<TConnection> reusePredicate,
      TimeProvider? timeProvider = null)
  {
    ArgumentNullException.ThrowIfNull(options);
    ArgumentNullException.ThrowIfNull(connectionFactory);
    ArgumentNullException.ThrowIfNull(reusePredicate);
    ValidateOptions(options);

    _options = options;
    _connectionFactory = connectionFactory;
    _reusePredicate = reusePredicate;
    _timeProvider = timeProvider ?? TimeProvider.System;
    _slots = new SemaphoreSlim(options.MaximumSize, options.MaximumSize);
    if (options.CleanerPeriod > TimeSpan.Zero &&
        (options.IdleTimeout != Timeout.InfiniteTimeSpan ||
         options.MaximumLifetime != Timeout.InfiniteTimeSpan))
    {
      _cleaner = _timeProvider.CreateTimer(
          static state => ((SqlConnectionPool<TConnection>)state!).ScheduleClean(),
          this,
          options.CleanerPeriod,
          options.CleanerPeriod);
    }
  }

  public int Size => Volatile.Read(ref _size);

  public async ValueTask<ISqlConnection> GetConnectionAsync(
      CancellationToken cancellationToken = default)
  {
    ThrowIfDisposed();
    await AcquireSlotAsync(cancellationToken).ConfigureAwait(false);

    try
    {
      ThrowIfDisposed();
      while (TryTakeIdle(out PooledConnection? candidate))
      {
        PooledConnection pooled = candidate!;
        if (IsReusable(pooled))
        {
          return new Lease(this, pooled);
        }

        await DisposeConnectionAsync(pooled.Connection).ConfigureAwait(false);
      }

      using CancellationTokenSource factoryCancellation =
        CancellationTokenSource.CreateLinkedTokenSource(
          cancellationToken,
          _disposeCancellation.Token);
      TConnection connection =
          await _connectionFactory(factoryCancellation.Token).ConfigureAwait(false);
      Interlocked.Increment(ref _size);
      if (_disposed != 0)
      {
        await DisposeConnectionAsync(connection).ConfigureAwait(false);
        throw new ObjectDisposedException(GetType().Name);
      }

      return new Lease(this, new PooledConnection(connection, _timeProvider.GetUtcNow()));
    }
    catch
    {
      _slots.Release();
      throw;
    }
  }

  public ValueTask<SqlRowSet> QueryAsync(
      string sql,
      CancellationToken cancellationToken = default) =>
      WithConnectionCoreAsync(
          (connection, token) => connection.QueryAsync(sql, token),
          cancellationToken);

  public ValueTask<SqlRowSet> QueryAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default) =>
      WithConnectionCoreAsync(
          (connection, token) => connection.QueryAsync(sql, parameters, token),
          cancellationToken);

  public ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      CancellationToken cancellationToken = default) =>
      WithConnectionCoreAsync(
          (connection, token) => connection.ExecuteAsync(sql, token),
          cancellationToken);

  public ValueTask<SqlCommandResult> ExecuteAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken = default) =>
      WithConnectionCoreAsync(
          (connection, token) => connection.ExecuteAsync(sql, parameters, token),
          cancellationToken);

  public async IAsyncEnumerable<SqlRow> StreamAsync(
      string sql,
      SqlParameters parameters = default,
      int fetchSize = 50,
      [EnumeratorCancellation] CancellationToken cancellationToken = default)
  {
    await using ISqlConnection connection =
        await GetConnectionAsync(cancellationToken).ConfigureAwait(false);
    await foreach (SqlRow row in connection.StreamAsync(
                       sql,
                       parameters,
                       fetchSize,
                       cancellationToken).ConfigureAwait(false))
    {
      yield return row;
    }
  }

  public async ValueTask DisposeAsync()
  {
    if (Interlocked.Exchange(ref _disposed, 1) != 0)
    {
      return;
    }

    await _disposeCancellation.CancelAsync().ConfigureAwait(false);
    if (_cleaner is not null)
    {
      await _cleaner.DisposeAsync().ConfigureAwait(false);
    }

    List<PooledConnection> idle;
    lock (_idleGate)
    {
      idle = [.. _idle];
      _idle.Clear();
    }

    foreach (PooledConnection pooled in idle)
    {
      await DisposeConnectionAsync(pooled.Connection).ConfigureAwait(false);
    }

    _disposeCancellation.Dispose();
  }

  internal async Task CleanAsync()
  {
    List<PooledConnection> expired = [];
    lock (_idleGate)
    {
      if (_disposed != 0)
      {
        return;
      }

      if (_idle.Count == 0)
      {
        return;
      }

      List<PooledConnection> reusable = new(_idle.Count);
      while (_idle.TryPop(out PooledConnection? pooled))
      {
        if (IsReusable(pooled))
        {
          reusable.Add(pooled);
        }
        else
        {
          expired.Add(pooled);
        }
      }

      foreach (PooledConnection pooled in reusable)
      {
        _idle.Push(pooled);
      }
    }

    foreach (PooledConnection pooled in expired)
    {
      await DisposeConnectionAsync(pooled.Connection).ConfigureAwait(false);
    }
  }

  private async ValueTask AcquireSlotAsync(CancellationToken cancellationToken)
  {
    if (_slots.Wait(0))
    {
      return;
    }

    int waiters = Interlocked.Increment(ref _waiters);
    if (_options.MaximumWaitQueueSize >= 0 &&
        waiters > _options.MaximumWaitQueueSize)
    {
      Interlocked.Decrement(ref _waiters);
      throw new SqlClientException("The SQL pool wait queue is full.");
    }

    try
    {
      using CancellationTokenSource timeout = CancellationTokenSource.CreateLinkedTokenSource(
          cancellationToken,
          _disposeCancellation.Token);
      timeout.CancelAfter(_options.AcquisitionTimeout);
      try
      {
        await _slots.WaitAsync(timeout.Token).ConfigureAwait(false);
      }
      catch (OperationCanceledException) when (_disposeCancellation.IsCancellationRequested)
      {
        throw new ObjectDisposedException(GetType().Name);
      }
      catch (OperationCanceledException) when (!cancellationToken.IsCancellationRequested)
      {
        throw new TimeoutException(
            $"Timed out waiting {_options.AcquisitionTimeout} for a SQL connection.");
      }
    }
    finally
    {
      Interlocked.Decrement(ref _waiters);
    }
  }

  private async ValueTask<TResult> WithConnectionCoreAsync<TResult>(
      Func<ISqlConnection, CancellationToken, ValueTask<TResult>> operation,
      CancellationToken cancellationToken)
  {
    await using ISqlConnection connection =
        await GetConnectionAsync(cancellationToken).ConfigureAwait(false);
    return await operation(connection, cancellationToken).ConfigureAwait(false);
  }

  private bool TryTakeIdle(out PooledConnection? pooled)
  {
    lock (_idleGate)
    {
      return _idle.TryPop(out pooled);
    }
  }

  private async ValueTask ReturnAsync(PooledConnection pooled)
  {
    pooled.LastUsed = _timeProvider.GetUtcNow();
    bool returned = false;
    if (_disposed == 0 && IsReusable(pooled))
    {
      lock (_idleGate)
      {
        if (_disposed == 0)
        {
          _idle.Push(pooled);
          returned = true;
        }
      }
    }

    try
    {
      if (!returned)
      {
        await DisposeConnectionAsync(pooled.Connection).ConfigureAwait(false);
      }
    }
    finally
    {
      if (_disposed == 0)
      {
        _slots.Release();
      }
    }
  }

  private bool IsReusable(PooledConnection pooled)
  {
    DateTimeOffset now = _timeProvider.GetUtcNow();
    return _reusePredicate(pooled.Connection) &&
           !HasElapsed(_options.IdleTimeout, pooled.LastUsed, now) &&
           !HasElapsed(_options.MaximumLifetime, pooled.Created, now);
  }

  private async ValueTask DisposeConnectionAsync(TConnection connection)
  {
    try
    {
      await connection.DisposeAsync().ConfigureAwait(false);
    }
    finally
    {
      Interlocked.Decrement(ref _size);
    }
  }

  private void ScheduleClean()
  {
    Task cleaning = CleanAsync();
    if (!cleaning.IsCompletedSuccessfully)
    {
      _ = ObserveCleaningAsync(cleaning);
    }
  }

  private static async Task ObserveCleaningAsync(Task cleaning)
  {
    try
    {
      await cleaning.ConfigureAwait(false);
    }
    catch (Exception)
    {
      // Periodic cleanup cannot report failures to a caller.
    }
  }

  private void ThrowIfDisposed() =>
      ObjectDisposedException.ThrowIf(_disposed != 0, this);

  private static bool HasElapsed(
      TimeSpan timeout,
      DateTimeOffset start,
      DateTimeOffset now) =>
      timeout != Timeout.InfiniteTimeSpan && now - start >= timeout;

  private static void ValidateOptions(SqlPoolOptions options)
  {
    ArgumentOutOfRangeException.ThrowIfNegativeOrZero(options.MaximumSize);
    if (options.MaximumWaitQueueSize < -1)
    {
      throw new ArgumentOutOfRangeException(nameof(options.MaximumWaitQueueSize));
    }

    if (options.AcquisitionTimeout <= TimeSpan.Zero)
    {
      throw new ArgumentOutOfRangeException(nameof(options.AcquisitionTimeout));
    }
  }

  private sealed class PooledConnection(
      TConnection connection,
      DateTimeOffset created)
  {
    internal TConnection Connection { get; } = connection;

    internal DateTimeOffset Created { get; } = created;

    internal DateTimeOffset LastUsed { get; set; } = created;
  }

  private sealed class Lease : ISqlConnection
  {
    private readonly object _gate = new();
    private SqlConnectionPool<TConnection>? _pool;
    private PooledConnection? _pooled;
    private int _children;
    private bool _disposeRequested;
    private bool _returned;

    internal Lease(
        SqlConnectionPool<TConnection> pool,
        PooledConnection pooled)
    {
      _pool = pool;
      _pooled = pooled;
    }

    private TConnection Connection
    {
      get
      {
        lock (_gate)
        {
          ThrowIfUnavailable();
          return _pooled!.Connection;
        }
      }
    }

    public bool IsSecure => Connection.IsSecure;

    public DatabaseMetadata DatabaseMetadata => Connection.DatabaseMetadata;

    public async ValueTask<SqlRowSet> QueryAsync(
        string sql,
        CancellationToken cancellationToken = default)
    {
      TConnection connection = BeginChild();
      try
      {
        return await connection.QueryAsync(sql, cancellationToken).ConfigureAwait(false);
      }
      finally
      {
        await EndChildAsync().ConfigureAwait(false);
      }
    }

    public async ValueTask<SqlRowSet> QueryAsync(
        string sql,
        SqlParameters parameters,
        CancellationToken cancellationToken = default)
    {
      TConnection connection = BeginChild();
      try
      {
        return await connection.QueryAsync(sql, parameters, cancellationToken)
            .ConfigureAwait(false);
      }
      finally
      {
        await EndChildAsync().ConfigureAwait(false);
      }
    }

    public async ValueTask<SqlCommandResult> ExecuteAsync(
        string sql,
        CancellationToken cancellationToken = default)
    {
      TConnection connection = BeginChild();
      try
      {
        return await connection.ExecuteAsync(sql, cancellationToken).ConfigureAwait(false);
      }
      finally
      {
        await EndChildAsync().ConfigureAwait(false);
      }
    }

    public async ValueTask<SqlCommandResult> ExecuteAsync(
        string sql,
        SqlParameters parameters,
        CancellationToken cancellationToken = default)
    {
      TConnection connection = BeginChild();
      try
      {
        return await connection.ExecuteAsync(sql, parameters, cancellationToken)
            .ConfigureAwait(false);
      }
      finally
      {
        await EndChildAsync().ConfigureAwait(false);
      }
    }

    public async IAsyncEnumerable<SqlRow> StreamAsync(
        string sql,
        SqlParameters parameters = default,
        int fetchSize = 50,
        [EnumeratorCancellation] CancellationToken cancellationToken = default)
    {
      TConnection connection = BeginChild();
      try
      {
        await foreach (SqlRow row in connection.StreamAsync(
                           sql,
                           parameters,
                           fetchSize,
                           cancellationToken).ConfigureAwait(false))
        {
          yield return row;
        }
      }
      finally
      {
        await EndChildAsync().ConfigureAwait(false);
      }
    }

    public async ValueTask<ISqlPreparedStatement> PrepareAsync(
        string sql,
        CancellationToken cancellationToken = default)
    {
      TConnection connection = BeginChild();
      try
      {
        ISqlPreparedStatement statement =
            await connection.PrepareAsync(sql, cancellationToken).ConfigureAwait(false);
        return new LeasePreparedStatement(this, statement);
      }
      catch
      {
        await EndChildAsync().ConfigureAwait(false);
        throw;
      }
    }

    public async ValueTask<ISqlTransaction> BeginTransactionAsync(
        CancellationToken cancellationToken = default)
    {
      TConnection connection = BeginChild();
      try
      {
        ISqlTransaction transaction =
            await connection.BeginTransactionAsync(cancellationToken).ConfigureAwait(false);
        return new LeaseTransaction(this, transaction);
      }
      catch
      {
        await EndChildAsync().ConfigureAwait(false);
        throw;
      }
    }

    public async ValueTask DisposeAsync()
    {
      (SqlConnectionPool<TConnection>? Pool, PooledConnection? Pooled) target;
      lock (_gate)
      {
        if (_disposeRequested)
        {
          return;
        }

        _disposeRequested = true;
        target = DetachIfReady();
      }

      await ReturnAsync(target).ConfigureAwait(false);
    }

    private TConnection BeginChild()
    {
      lock (_gate)
      {
        ThrowIfUnavailable();
        _children++;
        return _pooled!.Connection;
      }
    }

    private void PinChild()
    {
      lock (_gate)
      {
        if (_returned || _pooled is null)
        {
          throw new ObjectDisposedException(nameof(Lease));
        }

        _children++;
      }
    }

    private async ValueTask EndChildAsync()
    {
      (SqlConnectionPool<TConnection>? Pool, PooledConnection? Pooled) target;
      lock (_gate)
      {
        if (_children <= 0)
        {
          throw new InvalidOperationException("The pool lease child count is corrupt.");
        }

        _children--;
        target = DetachIfReady();
      }

      await ReturnAsync(target).ConfigureAwait(false);
    }

    private void ThrowIfUnavailable()
    {
      if (_disposeRequested || _returned || _pooled is null)
      {
        throw new ObjectDisposedException(nameof(Lease));
      }
    }

    private (SqlConnectionPool<TConnection>? Pool, PooledConnection? Pooled) DetachIfReady()
    {
      if (!_disposeRequested || _children != 0 || _returned)
      {
        return default;
      }

      _returned = true;
      SqlConnectionPool<TConnection>? pool = _pool;
      PooledConnection? pooled = _pooled;
      _pool = null;
      _pooled = null;
      return (pool, pooled);
    }

    private static async ValueTask ReturnAsync(
        (SqlConnectionPool<TConnection>? Pool, PooledConnection? Pooled) target)
    {
      if (target.Pool is not null && target.Pooled is not null)
      {
        await target.Pool.ReturnAsync(target.Pooled).ConfigureAwait(false);
      }
    }

    private sealed class LeasePreparedStatement : ISqlPreparedStatement
    {
      private readonly Lease _lease;
      private readonly ISqlPreparedStatement _inner;
      private int _disposed;

      internal LeasePreparedStatement(Lease lease, ISqlPreparedStatement inner)
      {
        _lease = lease;
        _inner = inner;
      }

      public string Sql => _inner.Sql;

      public ValueTask<SqlRowSet> QueryAsync(
          SqlParameters parameters = default,
          CancellationToken cancellationToken = default) =>
          _inner.QueryAsync(parameters, cancellationToken);

      public ValueTask<SqlCommandResult> ExecuteAsync(
          SqlParameters parameters = default,
          CancellationToken cancellationToken = default) =>
          _inner.ExecuteAsync(parameters, cancellationToken);

      public ValueTask<IReadOnlyList<SqlCommandResult>> ExecuteBatchAsync(
          IReadOnlyList<SqlParameters> batch,
          CancellationToken cancellationToken = default) =>
          _inner.ExecuteBatchAsync(batch, cancellationToken);

      public async ValueTask<ISqlCursor> OpenCursorAsync(
          SqlParameters parameters = default,
          int fetchSize = 50,
          CancellationToken cancellationToken = default)
      {
        _lease.PinChild();
        try
        {
          ISqlCursor cursor = await _inner.OpenCursorAsync(
              parameters,
              fetchSize,
              cancellationToken).ConfigureAwait(false);
          return new LeaseCursor(_lease, cursor);
        }
        catch
        {
          await _lease.EndChildAsync().ConfigureAwait(false);
          throw;
        }
      }

      public async IAsyncEnumerable<SqlRow> StreamAsync(
          SqlParameters parameters = default,
          int fetchSize = 50,
          [EnumeratorCancellation] CancellationToken cancellationToken = default)
      {
        _lease.PinChild();
        try
        {
          await foreach (SqlRow row in _inner.StreamAsync(
                             parameters,
                             fetchSize,
                             cancellationToken).ConfigureAwait(false))
          {
            yield return row;
          }
        }
        finally
        {
          await _lease.EndChildAsync().ConfigureAwait(false);
        }
      }

      public async ValueTask DisposeAsync()
      {
        if (Interlocked.Exchange(ref _disposed, 1) != 0)
        {
          return;
        }

        try
        {
          await _inner.DisposeAsync().ConfigureAwait(false);
        }
        finally
        {
          await _lease.EndChildAsync().ConfigureAwait(false);
        }
      }

      private sealed class LeaseCursor : ISqlCursor
      {
        private readonly Lease _lease;
        private readonly ISqlCursor _inner;
        private int _disposed;

        internal LeaseCursor(Lease lease, ISqlCursor inner)
        {
          _lease = lease;
          _inner = inner;
        }

        public bool HasMore => _inner.HasMore;

        public IReadOnlyList<SqlColumn> Columns => _inner.Columns;

        public ValueTask<SqlRowSet> ReadAsync(
            int count,
            CancellationToken cancellationToken = default) =>
            _inner.ReadAsync(count, cancellationToken);

        public async ValueTask DisposeAsync()
        {
          if (Interlocked.Exchange(ref _disposed, 1) != 0)
          {
            return;
          }

          try
          {
            await _inner.DisposeAsync().ConfigureAwait(false);
          }
          finally
          {
            await _lease.EndChildAsync().ConfigureAwait(false);
          }
        }
      }
    }

    private sealed class LeaseTransaction : ISqlTransaction
    {
      private readonly Lease _lease;
      private readonly ISqlTransaction _inner;
      private int _released;

      internal LeaseTransaction(Lease lease, ISqlTransaction inner)
      {
        _lease = lease;
        _inner = inner;
      }

      public bool IsCompleted => _inner.IsCompleted;

      public async ValueTask CommitAsync(CancellationToken cancellationToken = default)
      {
        await _inner.CommitAsync(cancellationToken).ConfigureAwait(false);
        await ReleaseAsync().ConfigureAwait(false);
      }

      public async ValueTask RollbackAsync(CancellationToken cancellationToken = default)
      {
        await _inner.RollbackAsync(cancellationToken).ConfigureAwait(false);
        await ReleaseAsync().ConfigureAwait(false);
      }

      public async ValueTask DisposeAsync()
      {
        try
        {
          await _inner.DisposeAsync().ConfigureAwait(false);
        }
        finally
        {
          await ReleaseAsync().ConfigureAwait(false);
        }
      }

      private async ValueTask ReleaseAsync()
      {
        if (Interlocked.Exchange(ref _released, 1) == 0)
        {
          await _lease.EndChildAsync().ConfigureAwait(false);
        }
      }
    }
  }
}

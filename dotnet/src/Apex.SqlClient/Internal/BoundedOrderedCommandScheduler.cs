/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Collections.Concurrent;
using System.Threading.Channels;
using System.Threading.Tasks.Sources;

namespace Apex.SqlClient.Internal;

internal sealed class BoundedOrderedCommandScheduler : IAsyncDisposable
{
  private readonly Channel<ICommand> _commands;
  private readonly int _inFlightLimit;
  private readonly Func<Exception, bool> _isFatal;
  private readonly CancellationTokenSource _shutdown = new();
  private readonly Task _pump;
  private Exception? _terminalError;
  private int _disposed;
  private int _shutdownDisposed;

  public BoundedOrderedCommandScheduler(
    int inFlightLimit,
    int queueCapacity,
    Func<Exception, bool>? isFatal = null)
  {
    ArgumentOutOfRangeException.ThrowIfNegativeOrZero(inFlightLimit);
    ArgumentOutOfRangeException.ThrowIfNegativeOrZero(queueCapacity);

    _inFlightLimit = inFlightLimit;
    _isFatal = isFatal ?? (_ => false);
    _commands = Channel.CreateBounded<ICommand>(
      new BoundedChannelOptions(queueCapacity)
      {
        AllowSynchronousContinuations = false,
        FullMode = BoundedChannelFullMode.Wait,
        SingleReader = true,
        SingleWriter = false,
      });
    _pump = PumpAsync();
  }

  public bool IsStopped =>
    Volatile.Read(ref _disposed) != 0 ||
    Volatile.Read(ref _terminalError) is not null;

  public ValueTask<T> ExecuteAsync<T>(
    Func<CancellationToken, ValueTask> sendAsync,
    Func<CancellationToken, ValueTask<T>> receiveAsync,
    bool barrier = false,
    CancellationToken cancellationToken = default)
  {
    ArgumentNullException.ThrowIfNull(sendAsync);
    ArgumentNullException.ThrowIfNull(receiveAsync);

    if (Volatile.Read(ref _disposed) != 0)
    {
      return ValueTask.FromException<T>(
        new ObjectDisposedException(nameof(BoundedOrderedCommandScheduler)));
    }

    Exception? terminalError = Volatile.Read(ref _terminalError);
    if (terminalError is not null)
    {
      return ValueTask.FromException<T>(terminalError);
    }

    Command<T> command = Command<T>.Rent(
      this,
      sendAsync,
      receiveAsync,
      barrier,
      cancellationToken);
    ValueTask<T> completion = command.Completion;
    command.Enqueue(_commands.Writer);
    return completion;
  }

  public async ValueTask DisposeAsync()
  {
    if (Interlocked.Exchange(ref _disposed, 1) == 0)
    {
      Stop(new ObjectDisposedException(nameof(BoundedOrderedCommandScheduler)));
      _shutdown.Cancel();
    }

    await _pump.ConfigureAwait(false);
    if (Interlocked.Exchange(ref _shutdownDisposed, 1) == 0)
    {
      _shutdown.Dispose();
    }
  }

  private async Task PumpAsync()
  {
    ICommand? deferred = null;
    List<BatchEntry> batch = [];

    try
    {
      while (await TryGetNextAsync(deferred).ConfigureAwait(false) is { } next)
      {
        deferred = null;
        if (next.CancellationToken.IsCancellationRequested)
        {
          next.Cancel(next.Generation);
          continue;
        }

        batch.Add(new BatchEntry(next));
        if (!next.IsBarrier)
        {
          while (batch.Count < _inFlightLimit && _commands.Reader.TryRead(out ICommand? candidate))
          {
            if (candidate.IsBarrier)
            {
              deferred = candidate;
              break;
            }

            batch.Add(new BatchEntry(candidate));
          }
        }

        if (!await SendBatchAsync(batch).ConfigureAwait(false))
        {
          return;
        }

        if (!await ReceiveBatchAsync(batch).ConfigureAwait(false))
        {
          return;
        }

        batch.Clear();
      }
    }
    catch (OperationCanceledException) when (_shutdown.IsCancellationRequested)
    {
    }
    catch (Exception exception)
    {
      Stop(exception);
    }
    finally
    {
      Exception terminalError = GetTerminalError();
      deferred?.Fail(deferred.Generation, terminalError);
      FailBatch(batch, terminalError);
      while (_commands.Reader.TryRead(out ICommand? command))
      {
        command.Fail(command.Generation, terminalError);
      }
    }
  }

  private async ValueTask<ICommand?> TryGetNextAsync(ICommand? deferred)
  {
    if (deferred is not null)
    {
      return deferred;
    }

    while (await _commands.Reader.WaitToReadAsync(_shutdown.Token).ConfigureAwait(false))
    {
      if (_commands.Reader.TryRead(out ICommand? command))
      {
        return command;
      }
    }

    return null;
  }

  private async ValueTask<bool> SendBatchAsync(List<BatchEntry> batch)
  {
    foreach (BatchEntry entry in batch)
    {
      if (entry.Command.CancellationToken.IsCancellationRequested)
      {
        entry.CanceledBeforeSend = true;
        continue;
      }

      try
      {
        using CancellationTokenSource cancellation = CreateDelegateCancellation(entry.Command);
        await entry.Command.SendAsync(cancellation.Token).ConfigureAwait(false);
        entry.WasSent = true;
      }
      catch (Exception exception)
      {
        entry.SendError = exception;
        if (IsFatal(exception))
        {
          Stop(exception);
          FailBatch(batch, GetTerminalError());
          return false;
        }
      }
    }

    return true;
  }

  private async ValueTask<bool> ReceiveBatchAsync(List<BatchEntry> batch)
  {
    foreach (BatchEntry entry in batch)
    {
      if (entry.CanceledBeforeSend)
      {
        entry.Command.Cancel(entry.Generation);
        continue;
      }

      if (entry.SendError is { } sendError)
      {
        CompleteFromDelegateError(entry, sendError);
        continue;
      }

      if (!entry.WasSent)
      {
        continue;
      }

      try
      {
        using CancellationTokenSource cancellation = CreateDelegateCancellation(entry.Command);
        await entry.Command.ReceiveAsync(entry.Generation, cancellation.Token).ConfigureAwait(false);
      }
      catch (Exception exception)
      {
        if (IsFatal(exception))
        {
          Stop(exception);
          FailBatch(batch, GetTerminalError());
          return false;
        }

        CompleteFromDelegateError(entry, exception);
      }
    }

    return true;
  }

  private CancellationTokenSource CreateDelegateCancellation(ICommand command) =>
    CancellationTokenSource.CreateLinkedTokenSource(command.CancellationToken, _shutdown.Token);

  private bool IsFatal(Exception exception) => _isFatal(exception);

  private void CompleteFromDelegateError(BatchEntry entry, Exception exception)
  {
    Exception? terminalError = Volatile.Read(ref _terminalError);
    if (_shutdown.IsCancellationRequested && terminalError is not null)
    {
      entry.Command.Fail(entry.Generation, terminalError);
    }
    else if (exception is OperationCanceledException)
    {
      entry.Command.Cancel(entry.Generation);
    }
    else
    {
      entry.Command.Fail(entry.Generation, exception);
    }
  }

  private void Stop(Exception exception)
  {
    Interlocked.CompareExchange(ref _terminalError, exception, null);
    _commands.Writer.TryComplete();
  }

  private Exception GetTerminalError() =>
    Volatile.Read(ref _terminalError)
    ?? new InvalidOperationException("The command scheduler stopped unexpectedly.");

  private static void FailBatch(List<BatchEntry> batch, Exception exception)
  {
    foreach (BatchEntry entry in batch)
    {
      entry.Command.Fail(entry.Generation, exception);
    }
  }

  private interface ICommand
  {
    bool IsBarrier { get; }

    int Generation { get; }

    CancellationToken CancellationToken { get; }

    ValueTask SendAsync(CancellationToken cancellationToken);

    ValueTask ReceiveAsync(int generation, CancellationToken cancellationToken);

    void Cancel(int generation);

    void Fail(int generation, Exception exception);
  }

  private sealed class Command<T> : ICommand, IValueTaskSource<T>
  {
    private static readonly ConcurrentQueue<Command<T>> Pool = new();
    private const int MaximumPoolSize = 256;
    private static int _poolCount;
    private readonly Action _continueEnqueue;
    private readonly object _lifecycleLock = new();
    private ManualResetValueTaskSourceCore<T> _completion;
    private Func<CancellationToken, ValueTask>? _sendAsync;
    private Func<CancellationToken, ValueTask<T>>? _receiveAsync;
    private BoundedOrderedCommandScheduler? _scheduler;
    private CancellationToken _cancellationToken;
    private ValueTask _pendingWrite;
    private long _completionState;
    private int _generation;
    private int _pendingWriteGeneration;
    private bool _isBarrier;
    private bool _consumed;

    private Command()
    {
      _continueEnqueue = ContinueEnqueue;
    }

    public ValueTask<T> Completion => new(this, _completion.Version);

    public bool IsBarrier => _isBarrier;

    public int Generation => Volatile.Read(ref _generation);

    public CancellationToken CancellationToken => _cancellationToken;

    public ValueTask SendAsync(CancellationToken delegateCancellationToken) =>
      _sendAsync!(delegateCancellationToken);

    public async ValueTask ReceiveAsync(
      int generation,
      CancellationToken delegateCancellationToken)
    {
      T result = await _receiveAsync!(delegateCancellationToken).ConfigureAwait(false);
      if (TryBeginCompletion(generation))
      {
        _completion.SetResult(result);
      }
    }

    public void Cancel(int generation)
    {
      if (TryBeginCompletion(generation))
      {
        _completion.SetException(new OperationCanceledException(_cancellationToken));
      }
    }

    public void Fail(int generation, Exception exception)
    {
      if (TryBeginCompletion(generation))
      {
        _completion.SetException(exception);
      }
    }

    public T GetResult(short token)
    {
      lock (_lifecycleLock)
      {
        ValueTaskSourceStatus status = _completion.GetStatus(token);
        if (status == ValueTaskSourceStatus.Pending)
        {
          throw new InvalidOperationException("The command has not completed.");
        }

        if (_consumed)
        {
          throw new InvalidOperationException("A command ValueTask may only be consumed once.");
        }

        _consumed = true;
        try
        {
          return _completion.GetResult(token);
        }
        finally
        {
          _sendAsync = null;
          _receiveAsync = null;
          _scheduler = null;
          _cancellationToken = default;
          _pendingWrite = default;
          _pendingWriteGeneration = 0;
          _isBarrier = false;
          _completion.Reset();
          if (Interlocked.Increment(ref _poolCount) <= MaximumPoolSize)
          {
            Pool.Enqueue(this);
          }
          else
          {
            Interlocked.Decrement(ref _poolCount);
          }
        }
      }
    }

    public ValueTaskSourceStatus GetStatus(short token) => _completion.GetStatus(token);

    public void OnCompleted(
      Action<object?> continuation,
      object? state,
      short token,
      ValueTaskSourceOnCompletedFlags flags) =>
      _completion.OnCompleted(continuation, state, token, flags);

    public static Command<T> Rent(
      BoundedOrderedCommandScheduler scheduler,
      Func<CancellationToken, ValueTask> sendAsync,
      Func<CancellationToken, ValueTask<T>> receiveAsync,
      bool isBarrier,
      CancellationToken cancellationToken)
    {
      if (!Pool.TryDequeue(out Command<T>? command))
      {
        command = new Command<T>();
      }
      else
      {
        Interlocked.Decrement(ref _poolCount);
      }

      command.Initialize(scheduler, sendAsync, receiveAsync, isBarrier, cancellationToken);
      return command;
    }

    public void Enqueue(ChannelWriter<ICommand> writer)
    {
      int generation = Generation;
      ValueTask write;
      try
      {
        write = writer.WriteAsync(this, _cancellationToken);
      }
      catch (Exception exception)
      {
        Fail(generation, exception);
        return;
      }

      if (write.IsCompleted)
      {
        CompleteEnqueue(generation, write);
        return;
      }

      _pendingWrite = write;
      _pendingWriteGeneration = generation;
      write.ConfigureAwait(false).GetAwaiter().UnsafeOnCompleted(_continueEnqueue);
    }

    private void Initialize(
      BoundedOrderedCommandScheduler scheduler,
      Func<CancellationToken, ValueTask> sendAsync,
      Func<CancellationToken, ValueTask<T>> receiveAsync,
      bool isBarrier,
      CancellationToken cancellationToken)
    {
      lock (_lifecycleLock)
      {
        _completion.RunContinuationsAsynchronously = true;
        _scheduler = scheduler;
        _sendAsync = sendAsync;
        _receiveAsync = receiveAsync;
        _cancellationToken = cancellationToken;
        _pendingWrite = default;
        _pendingWriteGeneration = 0;
        _isBarrier = isBarrier;
        _consumed = false;
        int generation = unchecked(_generation + 1);
        Volatile.Write(ref _generation, generation);
        Volatile.Write(ref _completionState, GetIncompleteState(generation));
      }
    }

    private void ContinueEnqueue()
    {
      int generation = _pendingWriteGeneration;
      ValueTask write = _pendingWrite;
      _pendingWrite = default;
      _pendingWriteGeneration = 0;
      CompleteEnqueue(generation, write);
    }

    private void CompleteEnqueue(int generation, ValueTask write)
    {
      try
      {
        write.GetAwaiter().GetResult();
      }
      catch (OperationCanceledException) when (_cancellationToken.IsCancellationRequested)
      {
        Cancel(generation);
      }
      catch (ChannelClosedException)
      {
        Fail(generation, _scheduler!.GetTerminalError());
      }
      catch (Exception exception)
      {
        Fail(generation, exception);
      }
    }

    private bool TryBeginCompletion(int generation)
    {
      long incomplete = GetIncompleteState(generation);
      return Interlocked.CompareExchange(
        ref _completionState,
        incomplete | 1,
        incomplete) == incomplete;
    }

    private static long GetIncompleteState(int generation) => (long)(uint)generation << 1;
  }

  private sealed class BatchEntry(ICommand command)
  {
    public ICommand Command { get; } = command;

    public int Generation { get; } = command.Generation;

    public bool CanceledBeforeSend { get; set; }

    public bool WasSent { get; set; }

    public Exception? SendError { get; set; }
  }
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient.Internal;

namespace Apex.SqlClient.Tests;

[TestClass]
public sealed class BoundedOrderedCommandSchedulerTests
{
  [TestMethod]
  public void RejectsNonpositiveLimits()
  {
    Assert.ThrowsExactly<ArgumentOutOfRangeException>(() => new BoundedOrderedCommandScheduler(0, 1));
    Assert.ThrowsExactly<ArgumentOutOfRangeException>(() => new BoundedOrderedCommandScheduler(1, 0));
  }

  [TestMethod]
  public async Task PipelinesOnlyTheConfiguredNumberInSubmissionOrder()
  {
    await using BoundedOrderedCommandScheduler scheduler = new(3, 4);
    List<string> events = [];
    (ValueTask<int> blocker, TaskCompletionSource releasePump) = await HoldPumpAsync(scheduler);

    ValueTask<int> first = Execute(scheduler, 1, events: events);
    ValueTask<int> second = Execute(scheduler, 2, events: events);
    ValueTask<int> third = Execute(scheduler, 3, events: events);
    ValueTask<int> fourth = Execute(scheduler, 4, events: events);

    releasePump.SetResult();
    await blocker;

    CollectionAssert.AreEqual(
      new[] { 1, 2, 3, 4 },
      await Task.WhenAll(first.AsTask(), second.AsTask(), third.AsTask(), fourth.AsTask()));
    CollectionAssert.AreEqual(
      new[]
      {
        "send-1",
        "send-2",
        "send-3",
        "receive-1",
        "receive-2",
        "receive-3",
        "send-4",
        "receive-4",
      },
      events);
  }

  [TestMethod]
  public async Task ReceivesAndCompletesResultsInSubmissionOrder()
  {
    await using BoundedOrderedCommandScheduler scheduler = new(3, 3);
    List<int> receiveOrder = [];
    TaskCompletionSource firstReceiveStarted = NewGate();
    TaskCompletionSource releaseFirstReceive = NewGate();
    TaskCompletionSource releaseSecondReceive = NewGate();
    TaskCompletionSource releaseThirdReceive = NewGate();
    (ValueTask<int> blocker, TaskCompletionSource releasePump) = await HoldPumpAsync(scheduler);

    ValueTask<int> first = scheduler.ExecuteAsync(
      _ => ValueTask.CompletedTask,
      async cancellationToken =>
      {
        receiveOrder.Add(1);
        firstReceiveStarted.SetResult();
        await releaseFirstReceive.Task.WaitAsync(cancellationToken);
        return 10;
      });
    ValueTask<int> second = scheduler.ExecuteAsync(
      _ => ValueTask.CompletedTask,
      async cancellationToken =>
      {
        receiveOrder.Add(2);
        await releaseSecondReceive.Task.WaitAsync(cancellationToken);
        return 20;
      });
    ValueTask<int> third = scheduler.ExecuteAsync(
      _ => ValueTask.CompletedTask,
      async cancellationToken =>
      {
        receiveOrder.Add(3);
        await releaseThirdReceive.Task.WaitAsync(cancellationToken);
        return 30;
      });

    releaseSecondReceive.SetResult();
    releaseThirdReceive.SetResult();
    releasePump.SetResult();
    await blocker;
    await firstReceiveStarted.Task;
    Assert.AreEqual(1, receiveOrder.Count);
    Assert.IsFalse(second.IsCompleted);
    Assert.IsFalse(third.IsCompleted);

    releaseFirstReceive.SetResult();

    CollectionAssert.AreEqual(
      new[] { 10, 20, 30 },
      await Task.WhenAll(first.AsTask(), second.AsTask(), third.AsTask()));
    CollectionAssert.AreEqual(new[] { 1, 2, 3 }, receiveOrder);
  }

  [TestMethod]
  public async Task BarrierExecutesAloneBetweenBatches()
  {
    await using BoundedOrderedCommandScheduler scheduler = new(3, 5);
    List<string> events = [];
    (ValueTask<int> blocker, TaskCompletionSource releasePump) = await HoldPumpAsync(scheduler);

    ValueTask<int> first = Execute(scheduler, 1, events: events);
    ValueTask<int> second = Execute(scheduler, 2, events: events);
    ValueTask<int> barrier = Execute(scheduler, 3, events: events, barrier: true);
    ValueTask<int> fourth = Execute(scheduler, 4, events: events);
    ValueTask<int> fifth = Execute(scheduler, 5, events: events);

    releasePump.SetResult();
    await blocker;

    await Task.WhenAll(
      first.AsTask(),
      second.AsTask(),
      barrier.AsTask(),
      fourth.AsTask(),
      fifth.AsTask());
    CollectionAssert.AreEqual(
      new[]
      {
        "send-1",
        "send-2",
        "receive-1",
        "receive-2",
        "send-3",
        "receive-3",
        "send-4",
        "send-5",
        "receive-4",
        "receive-5",
      },
      events);
  }

  [TestMethod]
  public async Task CancellationBeforeSendSkipsTheCommand()
  {
    await using BoundedOrderedCommandScheduler scheduler = new(1, 3);
    List<string> events = [];
    using CancellationTokenSource cancellation = new();
    (ValueTask<int> blocker, TaskCompletionSource releasePump) = await HoldPumpAsync(scheduler);

    ValueTask<int> first = Execute(scheduler, 1, events: events);
    ValueTask<int> canceled = Execute(
      scheduler,
      2,
      events: events,
      cancellationToken: cancellation.Token);
    ValueTask<int> third = Execute(scheduler, 3, events: events);

    cancellation.Cancel();
    releasePump.SetResult();
    await blocker;

    await Assert.ThrowsExactlyAsync<OperationCanceledException>(
      async () =>
      {
        _ = await canceled;
      });
    CollectionAssert.AreEqual(
      new[] { 1, 3 },
      await Task.WhenAll(first.AsTask(), third.AsTask()));
    CollectionAssert.AreEqual(
      new[] { "send-1", "receive-1", "send-3", "receive-3" },
      events);
  }

  [TestMethod]
  public async Task FatalErrorFailsQueuedCommandsAndStopsTheScheduler()
  {
    BoundedOrderedCommandScheduler scheduler = new(1, 3, exception => exception is FatalTestException);
    FatalTestException fatal = new();
    (ValueTask<int> blocker, TaskCompletionSource releasePump) = await HoldPumpAsync(scheduler);

    ValueTask<int> first = scheduler.ExecuteAsync(
      _ => ValueTask.CompletedTask,
      _ => ValueTask.FromException<int>(fatal));
    ValueTask<int> second = Execute(scheduler, 2);
    ValueTask<int> third = Execute(scheduler, 3);

    releasePump.SetResult();
    await blocker;

    Assert.AreSame(fatal, await AssertValueTaskThrowsExactlyAsync<FatalTestException, int>(first));
    Assert.AreSame(fatal, await AssertValueTaskThrowsExactlyAsync<FatalTestException, int>(second));
    Assert.AreSame(fatal, await AssertValueTaskThrowsExactlyAsync<FatalTestException, int>(third));
    Assert.AreSame(
      fatal,
      await AssertValueTaskThrowsExactlyAsync<FatalTestException, int>(
        scheduler.ExecuteAsync(_ => ValueTask.CompletedTask, _ => ValueTask.FromResult(4))));

    await scheduler.DisposeAsync();
  }

  [TestMethod]
  public async Task NonfatalErrorAllowsLaterCommandsToComplete()
  {
    await using BoundedOrderedCommandScheduler scheduler =
      new(2, 2, exception => exception is FatalTestException);
    List<string> events = [];
    InvalidOperationException nonfatal = new();
    (ValueTask<int> blocker, TaskCompletionSource releasePump) = await HoldPumpAsync(scheduler);

    ValueTask<int> first = scheduler.ExecuteAsync(
      _ =>
      {
        events.Add("send-1");
        return ValueTask.CompletedTask;
      },
      _ =>
      {
        events.Add("receive-1");
        return ValueTask.FromException<int>(nonfatal);
      });
    ValueTask<int> second = Execute(scheduler, 2, events: events);

    releasePump.SetResult();
    await blocker;
    Assert.AreSame(
      nonfatal,
      await AssertValueTaskThrowsExactlyAsync<InvalidOperationException, int>(first));
    Assert.AreEqual(2, await second);
    CollectionAssert.AreEqual(
      new[] { "send-1", "send-2", "receive-1", "receive-2" },
      events);
  }

  [TestMethod]
  public async Task DisposalFailsInFlightAndQueuedCommandsDeterministically()
  {
    BoundedOrderedCommandScheduler scheduler = new(1, 2);
    TaskCompletionSource receiveStarted = NewGate();

    ValueTask<int> first = scheduler.ExecuteAsync(
      _ => ValueTask.CompletedTask,
      async cancellationToken =>
      {
        receiveStarted.SetResult();
        await Task.Delay(Timeout.InfiniteTimeSpan, cancellationToken);
        return 1;
      });
    ValueTask<int> second = Execute(scheduler, 2);

    await receiveStarted.Task;
    await scheduler.DisposeAsync();

    await AssertValueTaskThrowsExactlyAsync<ObjectDisposedException, int>(first);
    await AssertValueTaskThrowsExactlyAsync<ObjectDisposedException, int>(second);
    await AssertValueTaskThrowsExactlyAsync<ObjectDisposedException, int>(
      scheduler.ExecuteAsync(_ => ValueTask.CompletedTask, _ => ValueTask.FromResult(3)));
    await scheduler.DisposeAsync();
  }

  [TestMethod]
  public async Task ReusesCommandsAcrossSynchronousAsyncCanceledAndFaultedCompletions()
  {
    await using BoundedOrderedCommandScheduler scheduler = new(1, 1);

    for (int i = 0; i < 500; i++)
    {
      ValueTask<int> command = i % 2 == 0
        ? scheduler.ExecuteAsync(
          static _ => ValueTask.CompletedTask,
          _ => ValueTask.FromResult(i))
        : scheduler.ExecuteAsync(
          static async _ => await Task.Yield(),
          async _ =>
          {
            await Task.Yield();
            return i;
          });

      Assert.AreEqual(i, await command);
    }

    using CancellationTokenSource cancellation = new();
    cancellation.Cancel();
    await AssertValueTaskThrowsExactlyAsync<OperationCanceledException, int>(
      Execute(scheduler, 500, cancellationToken: cancellation.Token));

    InvalidOperationException failure = new();
    Assert.AreSame(
      failure,
      await AssertValueTaskThrowsExactlyAsync<InvalidOperationException, int>(
        scheduler.ExecuteAsync(
          static _ => ValueTask.CompletedTask,
          _ => ValueTask.FromException<int>(failure))));

    Assert.AreEqual(501, await Execute(scheduler, 501));
  }

  [TestMethod]
  public async Task ValueTaskCanOnlyBeConsumedOnceAndReuseRemainsValid()
  {
    await using BoundedOrderedCommandScheduler scheduler = new(1, 1);
    ValueTask<int> first = Execute(scheduler, 1);

    Assert.AreEqual(1, await first);
    await AssertValueTaskThrowsExactlyAsync<InvalidOperationException, int>(first);

    Assert.AreEqual(2, await Execute(scheduler, 2));
  }

  [TestMethod]
  public async Task CompletionContinuationsRunAsynchronously()
  {
    await using BoundedOrderedCommandScheduler scheduler = new(2, 2);
    using ManualResetEventSlim releaseContinuation = new();
    TaskCompletionSource continuationStarted = NewGate();
    ValueTask<int> first = Execute(scheduler, 1);
    ValueTask<int> second = Execute(scheduler, 2);

    first.ConfigureAwait(false).GetAwaiter().UnsafeOnCompleted(
      () =>
      {
        continuationStarted.SetResult();
        releaseContinuation.Wait();
      });

    Assert.AreEqual(2, await second.AsTask().WaitAsync(TimeSpan.FromSeconds(10)));
    await continuationStarted.Task.WaitAsync(TimeSpan.FromSeconds(10));
    releaseContinuation.Set();
    Assert.AreEqual(1, first.GetAwaiter().GetResult());
  }

  private static ValueTask<int> Execute(
    BoundedOrderedCommandScheduler scheduler,
    int value,
    Func<CancellationToken, ValueTask>? send = null,
    List<string>? events = null,
    bool barrier = false,
    CancellationToken cancellationToken = default) =>
    scheduler.ExecuteAsync(
      send
      ?? (_ =>
      {
        events?.Add($"send-{value}");
        return ValueTask.CompletedTask;
      }),
      _ =>
      {
        events?.Add($"receive-{value}");
        return ValueTask.FromResult(value);
      },
      barrier,
      cancellationToken);

  private static TaskCompletionSource NewGate() =>
    new(TaskCreationOptions.RunContinuationsAsynchronously);

  private static async Task<(ValueTask<int> Command, TaskCompletionSource Release)> HoldPumpAsync(
    BoundedOrderedCommandScheduler scheduler)
  {
    TaskCompletionSource started = NewGate();
    TaskCompletionSource release = NewGate();
    ValueTask<int> command = scheduler.ExecuteAsync(
      async cancellationToken =>
      {
        started.SetResult();
        await release.Task.WaitAsync(cancellationToken);
      },
      _ => ValueTask.FromResult(0),
      barrier: true);
    await started.Task;
    return (command, release);
  }

  private static async Task<TException> AssertValueTaskThrowsExactlyAsync<TException, TResult>(
    ValueTask<TResult> valueTask)
    where TException : Exception =>
    await Assert.ThrowsExactlyAsync<TException>(
      async () =>
      {
        _ = await valueTask;
      });

  private sealed class FatalTestException : Exception;
}

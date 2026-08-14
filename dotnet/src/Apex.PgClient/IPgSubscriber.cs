/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.PgClient;

/// <summary>Manages LISTEN/UNLISTEN subscriptions over a dedicated PostgreSQL connection.</summary>
public interface IPgSubscriber : IAsyncDisposable
{
  IAsyncEnumerable<PgNotification> Notifications { get; }

  IReadOnlyCollection<string> Channels { get; }

  ValueTask SubscribeAsync(string channel, CancellationToken cancellationToken = default);

  ValueTask UnsubscribeAsync(string channel, CancellationToken cancellationToken = default);
}

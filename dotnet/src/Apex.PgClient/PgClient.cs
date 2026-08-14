/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.PgClient;

public static class PgClient
{
  public static ValueTask<PgConnection> ConnectAsync(
      PgConnectOptions options,
      CancellationToken cancellationToken = default)
  {
    ArgumentNullException.ThrowIfNull(options);
    return PgConnection.ConnectAsync(options, cancellationToken);
  }

  public static ValueTask<PgConnection> ConnectAsync(
    string connectionString,
    CancellationToken cancellationToken = default) =>
    ConnectAsync(PgConnectOptions.Parse(connectionString), cancellationToken);

  public static ValueTask<PgSubscriber> SubscribeAsync(
    PgConnectOptions options,
    Func<int, TimeSpan?>? reconnectPolicy = null,
    CancellationToken cancellationToken = default) =>
    PgSubscriber.ConnectAsync(options, reconnectPolicy, cancellationToken);
}

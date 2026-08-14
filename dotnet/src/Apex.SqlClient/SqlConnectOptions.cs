/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

public abstract record SqlConnectOptions
{
  public string Host { get; init; } = "localhost";

  public int Port { get; init; }

  public string Username { get; init; } = string.Empty;

  public string Password { get; init; } = string.Empty;

  public string Database { get; init; } = string.Empty;

  public TimeSpan ConnectTimeout { get; init; } = TimeSpan.FromSeconds(30);

  public int ReconnectAttempts { get; init; }

  public TimeSpan ReconnectInterval { get; init; } = TimeSpan.FromSeconds(1);

  public bool CachePreparedStatements { get; init; }

  public int PreparedStatementCacheSize { get; init; } = 256;

  public int PreparedStatementCacheSqlLengthLimit { get; init; } = 2048;
}

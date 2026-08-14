/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.PgClient;

public sealed record PgProxyOptions
{
  public required PgProxyType Type { get; init; }

  public required string Host { get; init; }

  public required int Port { get; init; }

  public string? Username { get; init; }

  public string? Password { get; init; }
}

public enum PgProxyType
{
  HttpConnect,
  Socks4a,
  Socks5,
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers.Binary;
using System.Net;
using System.Net.Sockets;
using System.Security.Authentication;
using System.Text;

namespace Apex.PgClient.Internal;

internal static class PgProxyConnector
{
  private static readonly Encoding Utf8 = new UTF8Encoding(false, true);

  public static async ValueTask<NetworkStream> ConnectAsync(
    Socket socket,
    PgConnectOptions options,
    CancellationToken cancellationToken)
  {
    if (options.Proxy is null)
    {
      await socket.ConnectAsync(CreateTargetEndPoint(options), cancellationToken)
        .ConfigureAwait(false);
      return new NetworkStream(socket, ownsSocket: false);
    }

    PgProxyOptions proxy = options.Proxy;
    await socket.ConnectAsync(
      new DnsEndPoint(proxy.Host, proxy.Port),
      cancellationToken).ConfigureAwait(false);
    NetworkStream stream = new(socket, ownsSocket: false);
    try
    {
      switch (proxy.Type)
      {
        case PgProxyType.HttpConnect:
          await ConnectHttpAsync(stream, options, proxy, cancellationToken).ConfigureAwait(false);
          break;
        case PgProxyType.Socks4a:
          await ConnectSocks4aAsync(stream, options, proxy, cancellationToken).ConfigureAwait(false);
          break;
        case PgProxyType.Socks5:
          await ConnectSocks5Async(stream, options, proxy, cancellationToken).ConfigureAwait(false);
          break;
        default:
          throw new ArgumentOutOfRangeException(nameof(options), "Unknown PostgreSQL proxy type.");
      }

      return stream;
    }
    catch
    {
      await stream.DisposeAsync().ConfigureAwait(false);
      throw;
    }
  }

  private static EndPoint CreateTargetEndPoint(PgConnectOptions options) =>
    options.Host.Length > 0 && options.Host[0] == '/'
      ? new UnixDomainSocketEndPoint(
        Path.Combine(options.Host, $".s.PGSQL.{options.Port}"))
      : new DnsEndPoint(options.Host, options.Port);

  private static async ValueTask ConnectHttpAsync(
    Stream stream,
    PgConnectOptions options,
    PgProxyOptions proxy,
    CancellationToken cancellationToken)
  {
    string authority = IPAddress.TryParse(options.Host, out IPAddress? httpAddress) &&
                       httpAddress.AddressFamily == AddressFamily.InterNetworkV6
      ? $"[{options.Host}]:{options.Port}"
      : $"{options.Host}:{options.Port}";
    StringBuilder request = new StringBuilder()
      .Append("CONNECT ").Append(authority)
      .Append(" HTTP/1.1\r\nHost: ").Append(authority)
      .Append("\r\n");
    if (proxy.Username is not null)
    {
      string credentials = Convert.ToBase64String(
        Utf8.GetBytes(proxy.Username + ":" + (proxy.Password ?? string.Empty)));
      request.Append("Proxy-Authorization: Basic ").Append(credentials).Append("\r\n");
    }

    request.Append("\r\n");
    await stream.WriteAsync(Utf8.GetBytes(request.ToString()), cancellationToken)
      .ConfigureAwait(false);
    await stream.FlushAsync(cancellationToken).ConfigureAwait(false);

    byte[] response = new byte[16 * 1024];
    int length = 0;
    while (length < response.Length)
    {
      int read = await stream.ReadAsync(response.AsMemory(length, 1), cancellationToken)
        .ConfigureAwait(false);
      if (read == 0)
      {
        throw new EndOfStreamException("HTTP proxy closed during CONNECT.");
      }

      length += read;
      if (length >= 4 &&
          response[length - 4] == '\r' &&
          response[length - 3] == '\n' &&
          response[length - 2] == '\r' &&
          response[length - 1] == '\n')
      {
        break;
      }
    }

    string statusLine = Utf8.GetString(response, 0, length).Split("\r\n", 2)[0];
    if (!statusLine.StartsWith("HTTP/", StringComparison.OrdinalIgnoreCase) ||
        statusLine.Split(' ', StringSplitOptions.RemoveEmptyEntries).ElementAtOrDefault(1) != "200")
    {
      throw new IOException($"HTTP proxy CONNECT failed: {statusLine}");
    }
  }

  private static async ValueTask ConnectSocks4aAsync(
    Stream stream,
    PgConnectOptions options,
    PgProxyOptions proxy,
    CancellationToken cancellationToken)
  {
    byte[] host = Utf8.GetBytes(options.Host);
    byte[] user = Utf8.GetBytes(proxy.Username ?? string.Empty);
    byte[] request = new byte[10 + user.Length + host.Length];
    request[0] = 4;
    request[1] = 1;
    BinaryPrimitives.WriteUInt16BigEndian(request.AsSpan(2), checked((ushort)options.Port));
    request[7] = 1;
    user.CopyTo(request, 8);
    host.CopyTo(request, 9 + user.Length);
    await stream.WriteAsync(request, cancellationToken).ConfigureAwait(false);
    await stream.FlushAsync(cancellationToken).ConfigureAwait(false);

    byte[] response = new byte[8];
    await stream.ReadExactlyAsync(response, cancellationToken).ConfigureAwait(false);
    if (response[1] != 90)
    {
      throw new IOException($"SOCKS4a proxy rejected the connection with status {response[1]}.");
    }
  }

  private static async ValueTask ConnectSocks5Async(
    Stream stream,
    PgConnectOptions options,
    PgProxyOptions proxy,
    CancellationToken cancellationToken)
  {
    bool authenticate = proxy.Username is not null;
    byte[] greeting = authenticate
      ? new byte[] { 5, 2, 0, 2 }
      : new byte[] { 5, 1, 0 };
    await stream.WriteAsync(greeting, cancellationToken).ConfigureAwait(false);
    await stream.FlushAsync(cancellationToken).ConfigureAwait(false);
    byte[] selection = new byte[2];
    await stream.ReadExactlyAsync(selection, cancellationToken).ConfigureAwait(false);
    if (selection[0] != 5 || selection[1] == 0xFF)
    {
      throw new IOException("SOCKS5 proxy did not accept an authentication method.");
    }

    if (selection[1] == 2)
    {
      await AuthenticateSocks5Async(stream, proxy, cancellationToken).ConfigureAwait(false);
    }
    else if (selection[1] != 0)
    {
      throw new IOException($"SOCKS5 proxy selected unsupported method {selection[1]}.");
    }

    byte addressType;
    byte[] host;
    if (IPAddress.TryParse(options.Host, out IPAddress? address))
    {
      host = address.GetAddressBytes();
      addressType = address.AddressFamily == AddressFamily.InterNetwork ? (byte)1 : (byte)4;
    }
    else
    {
      host = Utf8.GetBytes(options.Host);
      addressType = 3;
    }

    if (addressType == 3 && host.Length > byte.MaxValue)
    {
      throw new ArgumentException("SOCKS5 target host is too long.", nameof(options));
    }

    int addressPrefixLength = addressType == 3 ? 1 : 0;
    byte[] request = new byte[6 + addressPrefixLength + host.Length];
    request[0] = 5;
    request[1] = 1;
    request[2] = 0;
    request[3] = addressType;
    int addressOffset = 4;
    if (addressType == 3)
    {
      request[addressOffset++] = checked((byte)host.Length);
    }

    host.CopyTo(request, addressOffset);
    BinaryPrimitives.WriteUInt16BigEndian(
      request.AsSpan(addressOffset + host.Length),
      checked((ushort)options.Port));
    await stream.WriteAsync(request, cancellationToken).ConfigureAwait(false);
    await stream.FlushAsync(cancellationToken).ConfigureAwait(false);

    byte[] header = new byte[4];
    await stream.ReadExactlyAsync(header, cancellationToken).ConfigureAwait(false);
    if (header[0] != 5 || header[1] != 0)
    {
      throw new IOException($"SOCKS5 proxy rejected the connection with status {header[1]}.");
    }

    int addressLength = header[3] switch
    {
      1 => 4,
      4 => 16,
      3 => await ReadLengthAsync(stream, cancellationToken).ConfigureAwait(false),
      _ => throw new IOException($"SOCKS5 proxy returned unknown address type {header[3]}."),
    };
    byte[] addressAndPort = new byte[addressLength + 2];
    await stream.ReadExactlyAsync(addressAndPort, cancellationToken).ConfigureAwait(false);
  }

  private static async ValueTask AuthenticateSocks5Async(
    Stream stream,
    PgProxyOptions proxy,
    CancellationToken cancellationToken)
  {
    byte[] username = Utf8.GetBytes(proxy.Username ?? string.Empty);
    byte[] password = Utf8.GetBytes(proxy.Password ?? string.Empty);
    if (username.Length > byte.MaxValue || password.Length > byte.MaxValue)
    {
      throw new ArgumentException("SOCKS5 proxy credentials are too long.", nameof(proxy));
    }

    byte[] request = new byte[3 + username.Length + password.Length];
    request[0] = 1;
    request[1] = checked((byte)username.Length);
    username.CopyTo(request, 2);
    request[2 + username.Length] = checked((byte)password.Length);
    password.CopyTo(request, 3 + username.Length);
    await stream.WriteAsync(request, cancellationToken).ConfigureAwait(false);
    await stream.FlushAsync(cancellationToken).ConfigureAwait(false);
    byte[] response = new byte[2];
    await stream.ReadExactlyAsync(response, cancellationToken).ConfigureAwait(false);
    if (response[1] != 0)
    {
      throw new AuthenticationException("SOCKS5 proxy authentication failed.");
    }
  }

  private static async ValueTask<int> ReadLengthAsync(
    Stream stream,
    CancellationToken cancellationToken)
  {
    byte[] length = new byte[1];
    await stream.ReadExactlyAsync(length, cancellationToken).ConfigureAwait(false);
    return length[0];
  }
}

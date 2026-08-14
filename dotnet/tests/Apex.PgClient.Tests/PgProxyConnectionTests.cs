/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers.Binary;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace Apex.PgClient.Tests;

[TestClass]
public sealed class PgProxyConnectionTests
{
  [TestMethod]
  public Task ConnectsThroughHttpProxy() =>
    ConnectThroughProxyAsync(
      PgProxyType.HttpConnect,
      RunHttpProxyAsync,
      "http-user",
      "http-pass");

  [TestMethod]
  public Task ConnectsThroughSocks4aProxy() =>
    ConnectThroughProxyAsync(
      PgProxyType.Socks4a,
      RunSocks4aProxyAsync,
      "socks4-user",
      null);

  [TestMethod]
  public Task ConnectsThroughAuthenticatedSocks5Proxy() =>
    ConnectThroughProxyAsync(
      PgProxyType.Socks5,
      RunSocks5ProxyAsync,
      "socks5-user",
      "socks5-pass");

  private static async Task ConnectThroughProxyAsync(
    PgProxyType type,
    Func<TcpListener, Task> proxyServer,
    string? username,
    string? password)
  {
    TcpListener listener = new(IPAddress.Loopback, 0);
    listener.Start();
    int proxyPort = ((IPEndPoint)listener.LocalEndpoint).Port;
    Task server = proxyServer(listener);
    try
    {
      await using PgConnection connection = await PgClient.ConnectAsync(new PgConnectOptions
      {
        Host = "database.internal",
        Port = 5432,
        Username = "user",
        Password = "pass",
        Database = "db",
        Proxy = new PgProxyOptions
        {
          Type = type,
          Host = "127.0.0.1",
          Port = proxyPort,
          Username = username,
          Password = password,
        },
      });

      Assert.AreEqual(16, connection.DatabaseMetadata.MajorVersion);
      await connection.DisposeAsync();
      await server;
    }
    finally
    {
      listener.Stop();
    }
  }

  private static async Task RunHttpProxyAsync(TcpListener listener)
  {
    using TcpClient client = await listener.AcceptTcpClientAsync();
    await using NetworkStream stream = client.GetStream();
    string request = await ReadHeadersAsync(stream);
    StringAssert.Contains(request, "CONNECT database.internal:5432 HTTP/1.1");
    StringAssert.Contains(
      request,
      "Proxy-Authorization: Basic " +
      Convert.ToBase64String(Encoding.UTF8.GetBytes("http-user:http-pass")));
    await stream.WriteAsync(Encoding.ASCII.GetBytes("HTTP/1.1 200 Connection established\r\n\r\n"));
    await CompletePostgreSqlHandshakeAsync(stream);
  }

  private static async Task RunSocks4aProxyAsync(TcpListener listener)
  {
    using TcpClient client = await listener.AcceptTcpClientAsync();
    await using NetworkStream stream = client.GetStream();
    byte[] header = new byte[8];
    await stream.ReadExactlyAsync(header);
    Assert.AreEqual(4, header[0]);
    Assert.AreEqual(1, header[1]);
    Assert.AreEqual(5432, BinaryPrimitives.ReadUInt16BigEndian(header.AsSpan(2)));
    Assert.AreEqual("socks4-user", await ReadCStringAsync(stream));
    Assert.AreEqual("database.internal", await ReadCStringAsync(stream));
    await stream.WriteAsync(new byte[] { 0, 90, 0, 0, 0, 0, 0, 0 });
    await CompletePostgreSqlHandshakeAsync(stream);
  }

  private static async Task RunSocks5ProxyAsync(TcpListener listener)
  {
    using TcpClient client = await listener.AcceptTcpClientAsync();
    await using NetworkStream stream = client.GetStream();
    byte[] greeting = new byte[4];
    await stream.ReadExactlyAsync(greeting);
    CollectionAssert.AreEqual(new byte[] { 5, 2, 0, 2 }, greeting);
    await stream.WriteAsync(new byte[] { 5, 2 });

    byte[] authHeader = new byte[2];
    await stream.ReadExactlyAsync(authHeader);
    byte[] username = new byte[authHeader[1]];
    await stream.ReadExactlyAsync(username);
    byte[] passwordLength = new byte[1];
    await stream.ReadExactlyAsync(passwordLength);
    byte[] password = new byte[passwordLength[0]];
    await stream.ReadExactlyAsync(password);
    Assert.AreEqual("socks5-user", Encoding.UTF8.GetString(username));
    Assert.AreEqual("socks5-pass", Encoding.UTF8.GetString(password));
    await stream.WriteAsync(new byte[] { 1, 0 });

    byte[] connect = new byte[5];
    await stream.ReadExactlyAsync(connect);
    Assert.AreEqual(5, connect[0]);
    Assert.AreEqual(1, connect[1]);
    Assert.AreEqual(3, connect[3]);
    byte[] host = new byte[connect[4]];
    await stream.ReadExactlyAsync(host);
    byte[] port = new byte[2];
    await stream.ReadExactlyAsync(port);
    Assert.AreEqual("database.internal", Encoding.UTF8.GetString(host));
    Assert.AreEqual(5432, BinaryPrimitives.ReadUInt16BigEndian(port));
    await stream.WriteAsync(new byte[] { 5, 0, 0, 1, 127, 0, 0, 1, 0, 0 });
    await CompletePostgreSqlHandshakeAsync(stream);
  }

  private static async Task CompletePostgreSqlHandshakeAsync(Stream stream)
  {
    byte[] startupLength = new byte[4];
    await stream.ReadExactlyAsync(startupLength);
    byte[] startup = new byte[BinaryPrimitives.ReadInt32BigEndian(startupLength) - 4];
    await stream.ReadExactlyAsync(startup);
    await WriteMessageAsync(stream, (byte)'R', Int32(0));
    await WriteMessageAsync(
      stream,
      (byte)'S',
      [.. CString("server_version"), .. CString("16.4")]);
    await WriteMessageAsync(stream, (byte)'K', [.. Int32(123), .. Int32(456)]);
    await WriteMessageAsync(stream, (byte)'Z', [(byte)'I']);
    byte[] terminate = new byte[5];
    await stream.ReadExactlyAsync(terminate);
    Assert.AreEqual((byte)'X', terminate[0]);
  }

  private static async Task<string> ReadHeadersAsync(Stream stream)
  {
    List<byte> bytes = [];
    while (bytes.Count < 16 * 1024)
    {
      byte[] next = new byte[1];
      await stream.ReadExactlyAsync(next);
      bytes.Add(next[0]);
      int count = bytes.Count;
      if (count >= 4 &&
          bytes[count - 4] == '\r' &&
          bytes[count - 3] == '\n' &&
          bytes[count - 2] == '\r' &&
          bytes[count - 1] == '\n')
      {
        return Encoding.ASCII.GetString(bytes.ToArray());
      }
    }

    throw new InvalidDataException("Proxy headers are too large.");
  }

  private static async Task<string> ReadCStringAsync(Stream stream)
  {
    List<byte> bytes = [];
    while (true)
    {
      byte[] next = new byte[1];
      await stream.ReadExactlyAsync(next);
      if (next[0] == 0)
      {
        return Encoding.UTF8.GetString(bytes.ToArray());
      }

      bytes.Add(next[0]);
    }
  }

  private static async Task WriteMessageAsync(Stream stream, byte type, byte[] payload)
  {
    byte[] frame = new byte[payload.Length + 5];
    frame[0] = type;
    BinaryPrimitives.WriteInt32BigEndian(frame.AsSpan(1), payload.Length + 4);
    payload.CopyTo(frame, 5);
    await stream.WriteAsync(frame);
    await stream.FlushAsync();
  }

  private static byte[] CString(string value) => [.. Encoding.UTF8.GetBytes(value), 0];

  private static byte[] Int32(int value)
  {
    byte[] bytes = new byte[4];
    BinaryPrimitives.WriteInt32BigEndian(bytes, value);
    return bytes;
  }
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers.Binary;
using System.Net;
using System.Net.Security;
using System.Net.Sockets;
using System.Security.Authentication;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

namespace Apex.PgClient.Tests;

[TestClass]
public sealed class PgTlsConnectionTests
{
  [TestMethod]
  public async Task NegotiatesTraditionalPostgreSqlTls()
  {
    using X509Certificate2 certificate = CreateCertificate();
    TcpListener listener = new(IPAddress.Loopback, 0);
    listener.Start();
    int port = ((IPEndPoint)listener.LocalEndpoint).Port;
    Task server = RunTlsServerAsync(listener, certificate, direct: false);

    await using PgConnection connection = await PgClient.ConnectAsync(new PgConnectOptions
    {
      Host = "127.0.0.1",
      Port = port,
      Username = "user",
      Password = "pass",
      Database = "db",
      SslMode = PgSslMode.Require,
    });

    Assert.IsTrue(connection.IsSecure);
    await connection.DisposeAsync();
    await server;
    listener.Stop();
  }

  [TestMethod]
  public async Task NegotiatesDirectTls()
  {
    using X509Certificate2 certificate = CreateCertificate();
    TcpListener listener = new(IPAddress.Loopback, 0);
    listener.Start();
    int port = ((IPEndPoint)listener.LocalEndpoint).Port;
    Task server = RunTlsServerAsync(listener, certificate, direct: true);

    await using PgConnection connection = await PgClient.ConnectAsync(new PgConnectOptions
    {
      Host = "127.0.0.1",
      Port = port,
      Username = "user",
      Password = "pass",
      Database = "db",
      SslMode = PgSslMode.Require,
      SslNegotiation = PgSslNegotiation.Direct,
    });

    Assert.IsTrue(connection.IsSecure);
    await connection.DisposeAsync();
    await server;
    listener.Stop();
  }

  [TestMethod]
  public async Task PreferredTlsFallsBackWhenServerDeclines()
  {
    TcpListener listener = new(IPAddress.Loopback, 0);
    listener.Start();
    int port = ((IPEndPoint)listener.LocalEndpoint).Port;
    Task server = RunDeclinedTlsServerAsync(listener);

    await using PgConnection connection = await PgClient.ConnectAsync(new PgConnectOptions
    {
      Host = "127.0.0.1",
      Port = port,
      Username = "user",
      Password = "pass",
      Database = "db",
      SslMode = PgSslMode.Prefer,
    });

    Assert.IsFalse(connection.IsSecure);
    await connection.DisposeAsync();
    await server;
    listener.Stop();
  }

  [TestMethod]
  public async Task AllowModeRetriesWithTlsWhenServerRequiresEncryption()
  {
    using X509Certificate2 certificate = CreateCertificate();
    TcpListener listener = new(IPAddress.Loopback, 0);
    listener.Start();
    int port = ((IPEndPoint)listener.LocalEndpoint).Port;
    Task server = RunAllowFallbackServerAsync(listener, certificate);

    await using PgConnection connection = await PgClient.ConnectAsync(new PgConnectOptions
    {
      Host = "127.0.0.1",
      Port = port,
      Username = "user",
      Password = "pass",
      Database = "db",
      SslMode = PgSslMode.Allow,
    });

    Assert.IsTrue(connection.IsSecure);
    await connection.DisposeAsync();
    await server;
    listener.Stop();
  }

  private static async Task RunTlsServerAsync(
    TcpListener listener,
    X509Certificate2 certificate,
    bool direct)
  {
    using TcpClient client = await listener.AcceptTcpClientAsync();
    await using NetworkStream network = client.GetStream();
    if (!direct)
    {
      byte[] sslRequest = new byte[8];
      await network.ReadExactlyAsync(sslRequest);
      Assert.AreEqual(8, BinaryPrimitives.ReadInt32BigEndian(sslRequest));
      Assert.AreEqual(80877103, BinaryPrimitives.ReadInt32BigEndian(sslRequest.AsSpan(4)));
      await network.WriteAsync(new byte[] { (byte)'S' });
      await network.FlushAsync();
    }

    await using SslStream tls = new(network, leaveInnerStreamOpen: false);
    await tls.AuthenticateAsServerAsync(
      new SslServerAuthenticationOptions
      {
        ServerCertificate = certificate,
        EnabledSslProtocols = SslProtocols.Tls12 | SslProtocols.Tls13,
        ApplicationProtocols = direct
          ? [new SslApplicationProtocol("postgresql")]
          : null,
      });
    await ReadStartupAsync(tls);
    await WriteStartupCompleteAsync(tls, direct ? "17.1" : "16.4");
    (byte type, _) = await ReadMessageAsync(tls);
    Assert.AreEqual((byte)'X', type);
  }

  private static async Task RunDeclinedTlsServerAsync(TcpListener listener)
  {
    using TcpClient client = await listener.AcceptTcpClientAsync();
    await using NetworkStream stream = client.GetStream();
    byte[] sslRequest = new byte[8];
    await stream.ReadExactlyAsync(sslRequest);
    await stream.WriteAsync(new byte[] { (byte)'N' });
    await stream.FlushAsync();
    await ReadStartupAsync(stream);
    await WriteStartupCompleteAsync(stream, "16.4");
    (byte type, _) = await ReadMessageAsync(stream);
    Assert.AreEqual((byte)'X', type);
  }

  private static async Task RunAllowFallbackServerAsync(
    TcpListener listener,
    X509Certificate2 certificate)
  {
    using (TcpClient plain = await listener.AcceptTcpClientAsync())
    {
      await using NetworkStream stream = plain.GetStream();
      await ReadStartupAsync(stream);
      byte[] error =
      [
        (byte)'S', .. CString("FATAL"),
        (byte)'C', .. CString("28000"),
        (byte)'M', .. CString("no pg_hba.conf entry for host, no encryption"),
        0,
      ];
      await WriteMessageAsync(stream, (byte)'E', error);
      (byte type, _) = await ReadMessageAsync(stream);
      Assert.AreEqual((byte)'X', type);
    }

    using TcpClient secure = await listener.AcceptTcpClientAsync();
    await using NetworkStream network = secure.GetStream();
    byte[] sslRequest = new byte[8];
    await network.ReadExactlyAsync(sslRequest);
    await network.WriteAsync(new byte[] { (byte)'S' });
    await network.FlushAsync();
    await using SslStream tls = new(network, leaveInnerStreamOpen: false);
    await tls.AuthenticateAsServerAsync(certificate);
    await ReadStartupAsync(tls);
    await WriteStartupCompleteAsync(tls, "16.4");
    (byte terminate, _) = await ReadMessageAsync(tls);
    Assert.AreEqual((byte)'X', terminate);
  }

  private static X509Certificate2 CreateCertificate()
  {
    using RSA rsa = RSA.Create(2048);
    CertificateRequest request = new(
      "CN=localhost",
      rsa,
      HashAlgorithmName.SHA256,
      RSASignaturePadding.Pkcs1);
    SubjectAlternativeNameBuilder names = new();
    names.AddDnsName("localhost");
    names.AddIpAddress(IPAddress.Loopback);
    request.CertificateExtensions.Add(names.Build());
    request.CertificateExtensions.Add(
      new X509BasicConstraintsExtension(false, false, 0, critical: true));
    request.CertificateExtensions.Add(
      new X509KeyUsageExtension(X509KeyUsageFlags.DigitalSignature, critical: true));
    return request.CreateSelfSigned(
      DateTimeOffset.UtcNow.AddMinutes(-5),
      DateTimeOffset.UtcNow.AddDays(1));
  }

  private static async Task ReadStartupAsync(Stream stream)
  {
    byte[] length = new byte[4];
    await stream.ReadExactlyAsync(length);
    byte[] payload = new byte[BinaryPrimitives.ReadInt32BigEndian(length) - 4];
    await stream.ReadExactlyAsync(payload);
    Assert.AreEqual(196608, BinaryPrimitives.ReadInt32BigEndian(payload));
  }

  private static async Task WriteStartupCompleteAsync(Stream stream, string version)
  {
    await WriteMessageAsync(stream, (byte)'R', Int32(0));
    await WriteMessageAsync(
      stream,
      (byte)'S',
      [.. CString("server_version"), .. CString(version)]);
    await WriteMessageAsync(stream, (byte)'K', [.. Int32(123), .. Int32(456)]);
    await WriteMessageAsync(stream, (byte)'Z', [(byte)'I']);
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

  private static async Task<(byte Type, byte[] Payload)> ReadMessageAsync(Stream stream)
  {
    byte[] header = new byte[5];
    await stream.ReadExactlyAsync(header);
    byte[] payload = new byte[BinaryPrimitives.ReadInt32BigEndian(header.AsSpan(1)) - 4];
    await stream.ReadExactlyAsync(payload);
    return (header[0], payload);
  }

  private static byte[] CString(string value) =>
    [.. System.Text.Encoding.UTF8.GetBytes(value), 0];

  private static byte[] Int32(int value)
  {
    byte[] bytes = new byte[4];
    BinaryPrimitives.WriteInt32BigEndian(bytes, value);
    return bytes;
  }
}

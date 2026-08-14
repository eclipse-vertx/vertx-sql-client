/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;
using Apex.PgClient.Internal;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;

namespace Apex.PgClient;

public sealed record PgConnectOptions : SqlConnectOptions
{
  public PgConnectOptions()
  {
    Port = 5432;
    Username = "user";
    Password = "pass";
    Database = "db";
  }

  public int PipeliningLimit { get; init; } = 256;

  public PgSslMode SslMode { get; init; } = PgSslMode.Disable;

  public PgSslNegotiation SslNegotiation { get; init; } = PgSslNegotiation.Postgres;

  public PgChannelBinding ChannelBinding { get; init; } = PgChannelBinding.Prefer;

  public bool UseLayer7Proxy { get; init; }

  public PgProxyOptions? Proxy { get; init; }

  public RemoteCertificateValidationCallback? CertificateValidationCallback { get; init; }

  public IReadOnlyList<X509Certificate2> ClientCertificates { get; init; } =
    Array.Empty<X509Certificate2>();

  public X509RevocationMode CertificateRevocationCheckMode { get; init; } =
    X509RevocationMode.NoCheck;

  public IReadOnlyDictionary<string, string> Properties { get; init; } =
      new Dictionary<string, string>(StringComparer.Ordinal)
      {
        ["application_name"] = "apex-pg-client",
        ["client_encoding"] = "UTF8",
        ["DateStyle"] = "ISO",
        ["IntervalStyle"] = "iso_8601",
        ["extra_float_digits"] = "2",
      };

  public static PgConnectOptions FromEnvironment()
  {
    PgConnectOptions options = new();
    return options with
    {
      Host = GetEnvironment("PGHOSTADDR") ?? GetEnvironment("PGHOST") ?? options.Host,
      Port = ParsePort(GetEnvironment("PGPORT"), options.Port),
      Database = GetEnvironment("PGDATABASE") ?? options.Database,
      Username = GetEnvironment("PGUSER") ?? options.Username,
      Password = GetEnvironment("PGPASSWORD") ?? options.Password,
      SslMode = ParseSslMode(GetEnvironment("PGSSLMODE"), options.SslMode),
      SslNegotiation = ParseSslNegotiation(
            GetEnvironment("PGSSLNEGOTIATION"),
            options.SslNegotiation),
      ChannelBinding = ParseChannelBinding(
            GetEnvironment("PGCHANNELBINDING"),
            options.ChannelBinding),
    };
  }

  public static PgConnectOptions Parse(string connectionString)
  {
    ArgumentException.ThrowIfNullOrWhiteSpace(connectionString);
    return connectionString.StartsWith("postgres://", StringComparison.OrdinalIgnoreCase) ||
           connectionString.StartsWith("postgresql://", StringComparison.OrdinalIgnoreCase)
      ? ParseUri(connectionString)
      : Apply(new PgConnectOptions(), PgConnectionStringParser.ParseKeywords(connectionString));
  }

  private static PgConnectOptions ParseUri(string connectionString)
  {
    if (!Uri.TryCreate(connectionString, UriKind.Absolute, out Uri? uri) ||
        uri.Scheme is not ("postgres" or "postgresql"))
    {
      throw new FormatException("Invalid PostgreSQL connection URI.");
    }

    PgConnectOptions options = new()
    {
      Host = uri.Host,
      Port = uri.IsDefaultPort ? 5432 : uri.Port,
      Database = Uri.UnescapeDataString(uri.AbsolutePath.TrimStart('/')),
    };
    if (options.Database.Length == 0)
    {
      options = options with { Database = new PgConnectOptions().Database };
    }

    if (uri.UserInfo.Length > 0)
    {
      int separator = uri.UserInfo.IndexOf(':');
      options = options with
      {
        Username = Uri.UnescapeDataString(
          separator < 0 ? uri.UserInfo : uri.UserInfo[..separator]),
        Password = separator < 0
          ? string.Empty
          : Uri.UnescapeDataString(uri.UserInfo[(separator + 1)..]),
      };
    }

    return Apply(options, PgConnectionStringParser.ParseQuery(uri.Query));
  }

  private static PgConnectOptions Apply(
    PgConnectOptions options,
    IReadOnlyDictionary<string, string> values)
  {
    Dictionary<string, string> properties =
      new(options.Properties, StringComparer.Ordinal);
    foreach ((string key, string value) in values)
    {
      switch (key.ToLowerInvariant())
      {
        case "host":
          options = options with { Host = value };
          break;
        case "port":
          options = options with { Port = ParsePort(value, options.Port) };
          break;
        case "user":
        case "username":
          options = options with { Username = value };
          break;
        case "password":
          options = options with { Password = value };
          break;
        case "database":
        case "dbname":
          options = options with { Database = value };
          break;
        case "sslmode":
          options = options with { SslMode = ParseSslMode(value, options.SslMode) };
          break;
        case "sslnegotiation":
          options = options with
          {
            SslNegotiation = ParseSslNegotiation(value, options.SslNegotiation),
          };
          break;
        case "channel_binding":
        case "channelbinding":
          options = options with
          {
            ChannelBinding = ParseChannelBinding(value, options.ChannelBinding),
          };
          break;
        case "pipelininglimit":
          options = options with
          {
            PipeliningLimit = ParsePositiveInt(value, key),
          };
          break;
        case "cachepreparedstatements":
          options = options with
          {
            CachePreparedStatements = bool.Parse(value),
          };
          break;
        default:
          properties[key] = value;
          break;
      }
    }

    return options with { Properties = properties };
  }

  private static string? GetEnvironment(string name) =>
      Environment.GetEnvironmentVariable(name) is { Length: > 0 } value ? value : null;

  private static int ParsePort(string? value, int fallback) =>
      value is null
          ? fallback
          : int.TryParse(value, out int port) && port is > 0 and <= ushort.MaxValue
              ? port
              : throw new FormatException($"Invalid PostgreSQL port '{value}'.");

  private static PgSslMode ParseSslMode(string? value, PgSslMode fallback) =>
      value is null ? fallback : PgEnumParser.Parse<PgSslMode>(value);

  private static PgSslNegotiation ParseSslNegotiation(string? value, PgSslNegotiation fallback) =>
      value is null ? fallback : PgEnumParser.Parse<PgSslNegotiation>(value);

  private static PgChannelBinding ParseChannelBinding(string? value, PgChannelBinding fallback) =>
      value is null ? fallback : PgEnumParser.Parse<PgChannelBinding>(value);

  private static int ParsePositiveInt(string value, string name) =>
    int.TryParse(value, out int parsed) && parsed > 0
      ? parsed
      : throw new FormatException($"Invalid PostgreSQL {name} value '{value}'.");
}

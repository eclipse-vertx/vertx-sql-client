/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.PgClient.Tests;

[TestClass]
[DoNotParallelize]
public sealed class PgConnectOptionsTests
{
  [TestMethod]
  public void UsesPostgreSqlDefaults()
  {
    PgConnectOptions options = new();

    Assert.AreEqual("localhost", options.Host);
    Assert.AreEqual(5432, options.Port);
    Assert.AreEqual(256, options.PipeliningLimit);
    Assert.AreEqual(PgSslMode.Disable, options.SslMode);
    Assert.AreEqual(PgChannelBinding.Prefer, options.ChannelBinding);
  }

  [TestMethod]
  public void ReadsEnvironment()
  {
    string? oldHost = Environment.GetEnvironmentVariable("PGHOST");
    string? oldPort = Environment.GetEnvironmentVariable("PGPORT");
    try
    {
      Environment.SetEnvironmentVariable("PGHOST", "database.example");
      Environment.SetEnvironmentVariable("PGPORT", "5544");

      PgConnectOptions options = PgConnectOptions.FromEnvironment();

      Assert.AreEqual("database.example", options.Host);
      Assert.AreEqual(5544, options.Port);
    }
    finally
    {
      Environment.SetEnvironmentVariable("PGHOST", oldHost);
      Environment.SetEnvironmentVariable("PGPORT", oldPort);
    }
  }

  [TestMethod]
  public void ParsesUri()
  {
    PgConnectOptions options = PgConnectOptions.Parse(
      "postgresql://app%20user:s%40cret@db.example:5544/app%20db" +
      "?sslmode=verify-full&channel_binding=require&application_name=tests");

    Assert.AreEqual("db.example", options.Host);
    Assert.AreEqual(5544, options.Port);
    Assert.AreEqual("app user", options.Username);
    Assert.AreEqual("s@cret", options.Password);
    Assert.AreEqual("app db", options.Database);
    Assert.AreEqual(PgSslMode.VerifyFull, options.SslMode);
    Assert.AreEqual(PgChannelBinding.Require, options.ChannelBinding);
    Assert.AreEqual("tests", options.Properties["application_name"]);
  }

  [TestMethod]
  public void ParsesKeywordConnectionString()
  {
    PgConnectOptions options = PgConnectOptions.Parse(
      "host=db.example port=5544 user='app user' password='s\\'ecret' " +
      "dbname=app sslmode=require pipelininglimit=32");

    Assert.AreEqual("db.example", options.Host);
    Assert.AreEqual(5544, options.Port);
    Assert.AreEqual("app user", options.Username);
    Assert.AreEqual("s'ecret", options.Password);
    Assert.AreEqual("app", options.Database);
    Assert.AreEqual(PgSslMode.Require, options.SslMode);
    Assert.AreEqual(32, options.PipeliningLimit);
  }

  [TestMethod]
  public void RejectsMalformedConnectionString()
  {
    Assert.ThrowsExactly<FormatException>(() => PgConnectOptions.Parse("host"));
    Assert.ThrowsExactly<FormatException>(() => PgConnectOptions.Parse("port=invalid"));
  }
}

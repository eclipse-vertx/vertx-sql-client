/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.PgClient.Internal;

namespace Apex.PgClient.Tests;

[TestClass]
public sealed class PgAuthenticationTests
{
  [TestMethod]
  public void ComputesPostgreSqlMd5Password()
  {
    string result = PgWireWriter.Md5Password("password", "user", [1, 2, 3, 4]);

    Assert.AreEqual("md5a3576f1ae039b8996bc4fc2720f9c71a", result);
  }

  [TestMethod]
  public void ScramRejectsServerNonceWithoutClientPrefix()
  {
    PgScramClient client = new("user", "password");

    Assert.ThrowsExactly<InvalidDataException>(() =>
        client.HandleServerFirst("r=wrongnonce,s=QSXCR+Q6sek8bf92,i=4096"));
  }

  [TestMethod]
  public void ScramPlusUsesTlsServerEndPointGs2Header()
  {
    PgScramClient client = new("user", "password", [1, 2, 3, 4]);

    Assert.IsTrue(client.ClientFirstMessage.StartsWith(
        "p=tls-server-end-point,,",
        StringComparison.Ordinal));
  }

  [TestMethod]
  public void ScramPreferAdvertisesDowngradeDetection()
  {
    PgScramClient client = new(
        "user",
        "password",
        channelBindingData: null,
        advertiseChannelBinding: true);

    Assert.IsTrue(client.ClientFirstMessage.StartsWith("y,,", StringComparison.Ordinal));
  }

  [TestMethod]
  public async Task RequiredChannelBindingRejectsPlainConnection()
  {
    PgConnectOptions options = new()
    {
      ChannelBinding = PgChannelBinding.Require,
      SslMode = PgSslMode.Disable,
    };

    await Assert.ThrowsExactlyAsync<ArgumentException>(
        () => PgClient.ConnectAsync(options).AsTask());
  }
}

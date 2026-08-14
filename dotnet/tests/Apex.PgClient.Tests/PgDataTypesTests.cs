/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Net;

namespace Apex.PgClient.Tests;

[TestClass]
public sealed class PgDataTypesTests
{
  [TestMethod]
  public void ValidatesMoneyScale()
  {
    Assert.AreEqual(12.34m, new PgMoney(12.34m).Value);
    Assert.ThrowsExactly<ArgumentOutOfRangeException>(() => new PgMoney(12.345m));
  }

  [TestMethod]
  public void ValidatesNetworkPrefixes()
  {
    PgInet inet = new(IPAddress.Parse("192.0.2.1"), 24);
    PgCidr cidr = new(IPAddress.Parse("2001:db8::"), 64);

    Assert.AreEqual(24, inet.PrefixLength);
    Assert.AreEqual(64, cidr.PrefixLength);
    Assert.ThrowsExactly<ArgumentOutOfRangeException>(
      () => new PgCidr(IPAddress.Parse("192.0.2.0"), 33));
  }
}

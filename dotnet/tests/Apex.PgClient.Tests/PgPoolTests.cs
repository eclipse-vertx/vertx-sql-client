/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;

namespace Apex.PgClient.Tests;

[TestClass]
public sealed class PgPoolTests
{
  [TestMethod]
  public void DelegatesPoolOptionValidationToSharedCore()
  {
    PgConnectOptions connectOptions = new();

    Assert.ThrowsExactly<ArgumentOutOfRangeException>(
        () => PgPool.Create(
            connectOptions,
            new SqlPoolOptions { MaximumSize = 0 }));
    Assert.ThrowsExactly<ArgumentOutOfRangeException>(
        () => PgPool.Create(
            connectOptions,
            new SqlPoolOptions { MaximumWaitQueueSize = -2 }));
    Assert.ThrowsExactly<ArgumentOutOfRangeException>(
        () => PgPool.Create(
            connectOptions,
            new SqlPoolOptions { AcquisitionTimeout = TimeSpan.Zero }));
  }
}

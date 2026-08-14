/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.Tests.Shared;

namespace Apex.PgClient.Tests;

[TestClass]
public sealed class PublicApiSnapshotTests
{
  [TestMethod]
  public void PostgreSqlApiMatchesApprovedSnapshot() =>
    PublicApiSnapshot.Verify(typeof(PgClient).Assembly, "Apex.PgClient.txt");
}

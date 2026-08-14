/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.Tests.Shared;

namespace Apex.SqlClient.Tests;

[TestClass]
public sealed class PublicApiSnapshotTests
{
  [TestMethod]
  public void SharedApiMatchesApprovedSnapshot() =>
    PublicApiSnapshot.Verify(typeof(ISqlClient).Assembly, "Apex.SqlClient.txt");
}

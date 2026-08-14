/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient.Tests;

[TestClass]
public sealed class SqlParametersTests
{
  [TestMethod]
  public void CreateCopiesInputArray()
  {
    SqlValue[] values = [1, "two", SqlValue.Null];

    SqlParameters parameters = SqlParameters.Create(values);
    values[0] = 42;

    Assert.AreEqual(3, parameters.Count);
    Assert.AreEqual(1, parameters[0].Get<int>());
    Assert.AreEqual("two", parameters[1].Get<string>());
    Assert.IsTrue(parameters[2].IsNull);
  }

  [TestMethod]
  public void StoresCommonScalarsWithoutObjectInput()
  {
    SqlParameters parameters = SqlParameters.Create(
      true,
      (short)2,
      3,
      4L,
      5.5f,
      6.5d,
      7.5m);

    Assert.AreEqual(SqlValueKind.Boolean, parameters[0].Kind);
    Assert.AreEqual(3, parameters[2].Get<int>());
    Assert.AreEqual(7.5m, parameters[6].Get<decimal>());
  }

  [TestMethod]
  public void DefaultValueIsEmpty()
  {
    SqlParameters parameters = default;

    Assert.AreEqual(0, parameters.Count);
    Assert.AreEqual(0, parameters.Count());
  }
}

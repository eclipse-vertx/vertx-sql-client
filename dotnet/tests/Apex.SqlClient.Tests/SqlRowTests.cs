/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient.Tests;

[TestClass]
public sealed class SqlRowTests
{
  private static readonly SqlColumn[] Columns =
  [
      new("id", 23, 4, -1, SqlDataFormat.Binary),
        new("message", 25, -1, -1, SqlDataFormat.Text),
    ];

  [TestMethod]
  public void GetsValuesByOrdinalAndName()
  {
    SqlRow row = new(Columns, [1, "hello"]);

    Assert.AreEqual(1, row.Get<int>(0));
    Assert.AreEqual("hello", row.Get<string>(row.GetOrdinal("message")));
    Assert.AreEqual("hello", row["message"]);
  }

  [TestMethod]
  public void NameLookupIsOrdinal()
  {
    SqlRow row = new(Columns, [1, "hello"]);

    Assert.ThrowsExactly<IndexOutOfRangeException>(() => row.GetOrdinal("MESSAGE"));
  }

  [TestMethod]
  public void NullValueCannotBeReadAsNonNullableValueType()
  {
    SqlRow row = new(Columns, [null, "hello"]);

    Assert.IsTrue(row.IsNull(0));
    Assert.ThrowsExactly<InvalidCastException>(() => row.Get<int>(0));
  }
}

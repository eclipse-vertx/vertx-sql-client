/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.PgClient.Internal;

namespace Apex.PgClient.Tests;

[TestClass]
public sealed class PgPayloadReaderTests
{
  [TestMethod]
  public void ReadsBigEndianValuesAndCString()
  {
    byte[] payload = [0x01, 0x02, 0x03, 0x04, 0x00, 0x02, (byte)'o', (byte)'k', 0];
    PgPayloadReader reader = new(payload);

    Assert.AreEqual(0x01020304, reader.ReadInt32());
    Assert.AreEqual(2, reader.ReadInt16());
    Assert.AreEqual("ok", reader.ReadCString());
    Assert.AreEqual(0, reader.Remaining);
  }

  [TestMethod]
  public void RejectsTruncatedValues()
  {
    Assert.ThrowsExactly<InvalidDataException>(() =>
    {
      PgPayloadReader reader = new([0x01, 0x02, 0x03]);
      _ = reader.ReadInt32();
    });
  }
}

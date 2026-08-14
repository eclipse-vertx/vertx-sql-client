/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers.Binary;
using Apex.PgClient.Internal;

namespace Apex.PgClient.Tests;

[TestClass]
public sealed class PgBinaryCodecTests
{
  [TestMethod]
  public void DecodesPrimitiveAndInfinityValues()
  {
    Assert.AreEqual(42, PgBinaryCodec.Decode(23, Int32(42)));
    Assert.AreEqual(DateOnly.MaxValue, PgBinaryCodec.Decode(1082, Int32(int.MaxValue)));
    Assert.AreEqual(DateTimeOffset.MinValue, PgBinaryCodec.Decode(1184, Int64(long.MinValue)));
  }

  [TestMethod]
  public void DecodesArbitraryNumeric()
  {
    byte[] numeric =
    [
      .. Int16(3),
      .. Int16(1),
      .. Int16(0),
      .. Int16(2),
      .. Int16(1),
      .. Int16(2345),
      .. Int16(6700),
    ];

    PgNumeric value = (PgNumeric)PgBinaryCodec.Decode(1700, numeric);

    Assert.AreEqual("12345.67", value.ToString());

    byte[] weighted =
    [
      .. Int16(1),
      .. Int16(1),
      .. Int16(0),
      .. Int16(0),
      .. Int16(1),
    ];
    Assert.AreEqual(
      "10000",
      ((PgNumeric)PgBinaryCodec.Decode(1700, weighted)).ToString());
  }

  [TestMethod]
  public void DecodesOneDimensionalArrayWithNull()
  {
    byte[] array =
    [
      .. Int32(1),
      .. Int32(1),
      .. Int32(23),
      .. Int32(3),
      .. Int32(1),
      .. Int32(4),
      .. Int32(1),
      .. Int32(-1),
      .. Int32(4),
      .. Int32(3),
    ];

    object?[] values = (object?[])PgBinaryCodec.Decode(1007, array);

    CollectionAssert.AreEqual(new object?[] { 1, null, 3 }, values);
  }

  [TestMethod]
  public void DecodesInterval()
  {
    byte[] interval =
    [
      .. Int64(3_600_000_123),
      .. Int32(2),
      .. Int32(14),
    ];

    PgInterval value = (PgInterval)PgBinaryCodec.Decode(1186, interval);

    Assert.AreEqual(new PgInterval(1, 2, 2, 1, 0, 0, 123), value);
  }

  [TestMethod]
  public void RejectsTruncatedValue()
  {
    Assert.ThrowsExactly<InvalidDataException>(() =>
      PgBinaryCodec.Decode(23, new byte[3]));
  }

  private static byte[] Int16(short value)
  {
    byte[] bytes = new byte[2];
    BinaryPrimitives.WriteInt16BigEndian(bytes, value);
    return bytes;
  }

  private static byte[] Int32(int value)
  {
    byte[] bytes = new byte[4];
    BinaryPrimitives.WriteInt32BigEndian(bytes, value);
    return bytes;
  }

  private static byte[] Int64(long value)
  {
    byte[] bytes = new byte[8];
    BinaryPrimitives.WriteInt64BigEndian(bytes, value);
    return bytes;
  }
}

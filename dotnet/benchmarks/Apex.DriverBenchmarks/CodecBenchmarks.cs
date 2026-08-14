/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers.Binary;
using System.Text;
using Apex.PgClient.Internal;
using BenchmarkDotNet.Attributes;

namespace Apex.DriverBenchmarks;

[MemoryDiagnoser]
public class CodecBenchmarks
{
  private readonly byte[] _numericText =
    Encoding.UTF8.GetBytes("123456789012345678901234567890.123456");
  private readonly byte[] _arrayText =
    Encoding.UTF8.GetBytes("""{"one,two",NULL,"three"}""");
  private readonly byte[] _numericBinary = CreateNumericBinary();

  [Benchmark(Baseline = true)]
  public object DecodeNumericText() => PgTextCodec.Decode(1700, _numericText);

  [Benchmark]
  public object DecodeNumericBinary() => PgBinaryCodec.Decode(1700, _numericBinary);

  [Benchmark]
  public object DecodeTextArray() => PgTextCodec.Decode(1009, _arrayText);

  private static byte[] CreateNumericBinary()
  {
    short[] values = [5, 2, 0, 6, 12, 3456, 7890, 1234, 5678];
    byte[] binary = new byte[values.Length * 2];
    for (int i = 0; i < values.Length; i++)
    {
      BinaryPrimitives.WriteInt16BigEndian(binary.AsSpan(i * 2), values[i]);
    }

    return binary;
  }
}

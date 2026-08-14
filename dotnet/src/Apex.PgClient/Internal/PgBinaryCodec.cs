/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers.Binary;
using System.Net;
using System.Numerics;
using System.Text;
using System.Text.Json;

namespace Apex.PgClient.Internal;

internal static class PgBinaryCodec
{
  private static readonly DateOnly PgDateEpoch = new(2000, 1, 1);
  private static readonly DateTime PgTimestampEpoch =
    new(2000, 1, 1, 0, 0, 0, DateTimeKind.Unspecified);
  private static readonly DateTimeOffset PgTimestampWithTimeZoneEpoch =
    new(2000, 1, 1, 0, 0, 0, TimeSpan.Zero);

  public static object Decode(uint typeId, ReadOnlySpan<byte> value) =>
    typeId switch
    {
      16 => ReadByte(value) != 0,
      17 => value.ToArray(),
      20 => ReadInt64(value),
      21 => ReadInt16(value),
      23 => ReadInt32(value),
      26 or 142 or 829 or 1560 or 1562 or 2278 or 774 => throw new PgUnsupportedTypeException(typeId),
      700 => BitConverter.Int32BitsToSingle(ReadInt32(value)),
      701 => BitConverter.Int64BitsToDouble(ReadInt64(value)),
      790 => new PgMoney(ReadInt64(value) / 100m),
      1082 => DecodeDate(value),
      1083 => TimeOnly.FromTimeSpan(TimeSpan.FromTicks(ReadInt64(value) * 10)),
      1114 => DecodeTimestamp(value),
      1184 => DecodeTimestampWithTimeZone(value),
      1186 => DecodeInterval(value),
      1266 => DecodeTimeWithTimeZone(value),
      1700 => DecodeNumeric(value),
      2950 => DecodeGuid(value),
      600 => DecodePoint(value),
      601 => new PgLineSegment(DecodePoint(value), DecodePoint(value[16..])),
      602 => DecodePath(value),
      603 => new PgBox(DecodePoint(value), DecodePoint(value[16..])),
      604 => new PgPolygon(DecodePoints(value, hasClosedFlag: false)),
      628 => DecodeLine(value),
      650 => DecodeCidr(value),
      718 => new PgCircle(DecodePoint(value), ReadDouble(value[16..])),
      869 => DecodeInet(value),
      114 => DecodeJson(value),
      3802 => DecodeJsonb(value),
      18 or 19 or 25 or 1042 or 1043 => Encoding.UTF8.GetString(value),
      1000 or 1001 or 1002 or 1003 or 1005 or 1007 or 1009 or 1015 or
      1016 or 1017 or 1018 or 1019 or 1020 or 1021 or 1022 or 1027 or
      1041 or 1115 or 1182 or 1183 or 1185 or 1187 or 1231 or 1270 or
      199 or 629 or 651 or 719 or 791 or 2951 or 3807 => DecodeArray(value),
      _ => throw new PgUnsupportedTypeException(typeId),
    };

  private static object?[] DecodeArray(ReadOnlySpan<byte> value)
  {
    int position = 0;
    int dimensions = ReadInt32(value, ref position);
    _ = ReadInt32(value, ref position);
    uint elementType = unchecked((uint)ReadInt32(value, ref position));
    if (dimensions == 0)
    {
      return [];
    }

    if (dimensions != 1)
    {
      throw new NotSupportedException("Multidimensional PostgreSQL arrays are not supported yet.");
    }

    int count = ReadInt32(value, ref position);
    _ = ReadInt32(value, ref position);
    if (count < 0 || count > (value.Length - position) / sizeof(int))
    {
      throw new InvalidDataException("PostgreSQL array element count exceeds its payload.");
    }

    object?[] result = new object?[count];
    for (int i = 0; i < count; i++)
    {
      int length = ReadInt32(value, ref position);
      if (length < 0)
      {
        continue;
      }

      Ensure(value, position, length);
      result[i] = Decode(elementType, value.Slice(position, length));
      position += length;
    }

    return result;
  }

  private static PgNumeric DecodeNumeric(ReadOnlySpan<byte> value)
  {
    int position = 0;
    int digitCount = ReadInt16(value, ref position);
    int weight = ReadInt16(value, ref position);
    ushort sign = unchecked((ushort)ReadInt16(value, ref position));
    int displayScale = unchecked((ushort)ReadInt16(value, ref position));
    if (sign == 0xC000)
    {
      return PgNumeric.NaN;
    }

    if (sign == 0xD000)
    {
      return PgNumeric.PositiveInfinity;
    }

    if (sign == 0xF000)
    {
      return PgNumeric.NegativeInfinity;
    }

    BigInteger coefficient = BigInteger.Zero;
    for (int i = 0; i < digitCount; i++)
    {
      int digit = unchecked((ushort)ReadInt16(value, ref position));
      if (digit > 9999)
      {
        throw new InvalidDataException("Invalid PostgreSQL numeric base-10000 digit.");
      }

      coefficient = (coefficient * 10000) + digit;
    }

    int fractionalGroups = digitCount - weight - 1;
    if (fractionalGroups < 0)
    {
      coefficient *= BigInteger.Pow(10000, -fractionalGroups);
      fractionalGroups = 0;
    }

    int scale = Math.Max(0, fractionalGroups * 4);
    if (scale > displayScale)
    {
      coefficient /= BigInteger.Pow(10, scale - displayScale);
      scale = displayScale;
    }
    else if (scale < displayScale)
    {
      coefficient *= BigInteger.Pow(10, displayScale - scale);
      scale = displayScale;
    }

    if (sign == 0x4000)
    {
      coefficient = -coefficient;
    }

    return PgNumeric.Create(coefficient, scale);
  }

  private static JsonElement DecodeJsonb(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 1);
    if (value[0] != 1)
    {
      throw new InvalidDataException($"Unsupported PostgreSQL jsonb version {value[0]}.");
    }

    return DecodeJson(value[1..]);
  }

  private static JsonElement DecodeJson(ReadOnlySpan<byte> value)
  {
    using JsonDocument document = JsonDocument.Parse(value.ToArray());
    return document.RootElement.Clone();
  }

  private static PgInterval DecodeInterval(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 16);
    long microseconds = ReadInt64(value);
    int days = ReadInt32(value[8..]);
    int months = ReadInt32(value[12..]);
    long seconds = Math.DivRem(microseconds, 1_000_000, out long remainingMicros);
    long hours = Math.DivRem(seconds, 3600, out long remainingSeconds);
    long minutes = Math.DivRem(remainingSeconds, 60, out long finalSeconds);
    return new PgInterval(
      months / 12,
      months % 12,
      days,
      checked((int)hours),
      checked((int)minutes),
      checked((int)finalSeconds),
      checked((int)remainingMicros));
  }

  private static DateOnly DecodeDate(ReadOnlySpan<byte> value)
  {
    int days = ReadInt32(value);
    return days switch
    {
      int.MaxValue => DateOnly.MaxValue,
      int.MinValue => DateOnly.MinValue,
      _ => PgDateEpoch.AddDays(days),
    };
  }

  private static DateTime DecodeTimestamp(ReadOnlySpan<byte> value)
  {
    long microseconds = ReadInt64(value);
    return microseconds switch
    {
      long.MaxValue => DateTime.MaxValue,
      long.MinValue => DateTime.MinValue,
      _ => PgTimestampEpoch.AddTicks(microseconds * 10),
    };
  }

  private static DateTimeOffset DecodeTimestampWithTimeZone(ReadOnlySpan<byte> value)
  {
    long microseconds = ReadInt64(value);
    return microseconds switch
    {
      long.MaxValue => DateTimeOffset.MaxValue,
      long.MinValue => DateTimeOffset.MinValue,
      _ => PgTimestampWithTimeZoneEpoch.AddTicks(microseconds * 10),
    };
  }

  private static PgTimeWithTimeZone DecodeTimeWithTimeZone(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 12);
    TimeOnly time = TimeOnly.FromTimeSpan(TimeSpan.FromTicks(ReadInt64(value) * 10));
    TimeSpan offset = TimeSpan.FromSeconds(-ReadInt32(value[8..]));
    return new PgTimeWithTimeZone(time, offset);
  }

  private static Guid DecodeGuid(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 16);
    Span<byte> guid = stackalloc byte[16];
    value.CopyTo(guid);
    if (BitConverter.IsLittleEndian)
    {
      guid[..4].Reverse();
      guid.Slice(4, 2).Reverse();
      guid.Slice(6, 2).Reverse();
    }

    return new Guid(guid);
  }

  private static PgPoint DecodePoint(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 16);
    return new PgPoint(ReadDouble(value), ReadDouble(value[8..]));
  }

  private static PgLine DecodeLine(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 24);
    return new PgLine(ReadDouble(value), ReadDouble(value[8..]), ReadDouble(value[16..]));
  }

  private static PgPath DecodePath(ReadOnlySpan<byte> value) =>
    new(DecodePoints(value, hasClosedFlag: true, out bool closed), closed);

  private static PgPoint[] DecodePoints(ReadOnlySpan<byte> value, bool hasClosedFlag) =>
    DecodePoints(value, hasClosedFlag, out _);

  private static PgPoint[] DecodePoints(
    ReadOnlySpan<byte> value,
    bool hasClosedFlag,
    out bool closed)
  {
    int position = 0;
    closed = hasClosedFlag && ReadByte(value, ref position) != 0;
    int count = ReadInt32(value, ref position);
    if (count < 0 || count > (value.Length - position) / 16)
    {
      throw new InvalidDataException("PostgreSQL point count exceeds its payload.");
    }

    PgPoint[] points = new PgPoint[count];
    for (int i = 0; i < count; i++)
    {
      Ensure(value, position, 16);
      points[i] = DecodePoint(value[position..]);
      position += 16;
    }

    return points;
  }

  private static PgInet DecodeInet(ReadOnlySpan<byte> value)
  {
    (IPAddress address, int prefix, _) = DecodeNetwork(value);
    return new PgInet(address, prefix);
  }

  private static PgCidr DecodeCidr(ReadOnlySpan<byte> value)
  {
    (IPAddress address, int prefix, _) = DecodeNetwork(value);
    return new PgCidr(address, prefix);
  }

  private static (IPAddress Address, int Prefix, bool Cidr) DecodeNetwork(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 4);
    int addressLength = value[3];
    Ensure(value, 4, addressLength);
    return (new IPAddress(value.Slice(4, addressLength)), value[1], value[2] != 0);
  }

  private static byte ReadByte(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 1);
    return value[0];
  }

  private static byte ReadByte(ReadOnlySpan<byte> value, ref int position)
  {
    Ensure(value, position, 1);
    return value[position++];
  }

  private static short ReadInt16(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 2);
    return BinaryPrimitives.ReadInt16BigEndian(value);
  }

  private static short ReadInt16(ReadOnlySpan<byte> value, ref int position)
  {
    Ensure(value, position, 2);
    short result = BinaryPrimitives.ReadInt16BigEndian(value[position..]);
    position += 2;
    return result;
  }

  private static int ReadInt32(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 4);
    return BinaryPrimitives.ReadInt32BigEndian(value);
  }

  private static int ReadInt32(ReadOnlySpan<byte> value, ref int position)
  {
    Ensure(value, position, 4);
    int result = BinaryPrimitives.ReadInt32BigEndian(value[position..]);
    position += 4;
    return result;
  }

  private static long ReadInt64(ReadOnlySpan<byte> value)
  {
    Ensure(value, 0, 8);
    return BinaryPrimitives.ReadInt64BigEndian(value);
  }

  private static double ReadDouble(ReadOnlySpan<byte> value) =>
    BitConverter.Int64BitsToDouble(ReadInt64(value));

  private static void Ensure(ReadOnlySpan<byte> value, int position, int length)
  {
    if (length < 0 || position < 0 || position > value.Length - length)
    {
      throw new InvalidDataException("PostgreSQL binary value is truncated.");
    }
  }
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers.Binary;
using System.Text;

namespace Apex.PgClient.Internal;

internal ref struct PgPayloadReader
{
  private static readonly Encoding Utf8 = new UTF8Encoding(false, true);
  private readonly ReadOnlySpan<byte> _payload;
  private int _position;

  public PgPayloadReader(ReadOnlySpan<byte> payload)
  {
    _payload = payload;
  }

  public int Remaining => _payload.Length - _position;

  public byte ReadByte()
  {
    Ensure(sizeof(byte));
    return _payload[_position++];
  }

  public short ReadInt16()
  {
    Ensure(sizeof(short));
    short value = BinaryPrimitives.ReadInt16BigEndian(_payload[_position..]);
    _position += sizeof(short);
    return value;
  }

  public int ReadInt32()
  {
    Ensure(sizeof(int));
    int value = BinaryPrimitives.ReadInt32BigEndian(_payload[_position..]);
    _position += sizeof(int);
    return value;
  }

  public string ReadCString()
  {
    ReadOnlySpan<byte> remaining = _payload[_position..];
    int length = remaining.IndexOf((byte)0);
    if (length < 0)
    {
      throw new InvalidDataException("PostgreSQL string is not null terminated.");
    }

    string value = Utf8.GetString(remaining[..length]);
    _position += length + 1;
    return value;
  }

  public string ReadString(int length)
  {
    Ensure(length);
    string value = Utf8.GetString(_payload.Slice(_position, length));
    _position += length;
    return value;
  }

  public byte[] ReadBytes(int length)
  {
    return ReadSpan(length).ToArray();
  }

  public ReadOnlySpan<byte> ReadSpan(int length)
  {
    Ensure(length);
    ReadOnlySpan<byte> value = _payload.Slice(_position, length);
    _position += length;
    return value;
  }

  private void Ensure(int length)
  {
    if (length < 0 || _position > _payload.Length - length)
    {
      throw new InvalidDataException("PostgreSQL message payload is truncated.");
    }
  }
}

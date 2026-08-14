/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers;
using System.Buffers.Binary;
using System.IO.Pipelines;

namespace Apex.PgClient.Internal;

internal readonly struct PgWireMessage : IDisposable
{
  private readonly byte[]? _buffer;

  public PgWireMessage(byte type, byte[]? buffer, int payloadLength)
  {
    Type = type;
    _buffer = buffer;
    PayloadLength = payloadLength;
  }

  public byte Type { get; }

  public int PayloadLength { get; }

  public ReadOnlyMemory<byte> Payload =>
    _buffer is null ? ReadOnlyMemory<byte>.Empty : _buffer.AsMemory(0, PayloadLength);

  public void Dispose()
  {
    if (_buffer is not null)
    {
      ArrayPool<byte>.Shared.Return(_buffer);
    }
  }
}

internal sealed class PgWireReader
{
  private const int MaximumMessageLength = 64 * 1024 * 1024;
  private readonly PipeReader _reader;

  public PgWireReader(PipeReader reader)
  {
    _reader = reader;
  }

  public async ValueTask<PgWireMessage> ReadAsync(CancellationToken cancellationToken)
  {
    while (true)
    {
      ReadResult result = await _reader.ReadAsync(cancellationToken).ConfigureAwait(false);
      ReadOnlySequence<byte> buffer = result.Buffer;

      if (TryReadMessage(buffer, out PgWireMessage message, out SequencePosition consumed))
      {
        _reader.AdvanceTo(consumed);
        return message;
      }

      if (result.IsCompleted)
      {
        _reader.AdvanceTo(buffer.End);
        throw new EndOfStreamException("PostgreSQL closed the connection mid-message.");
      }

      _reader.AdvanceTo(buffer.Start, buffer.End);
    }
  }

  public ValueTask CompleteAsync(Exception? exception = null) => _reader.CompleteAsync(exception);

  private static bool TryReadMessage(
      ReadOnlySequence<byte> buffer,
      out PgWireMessage message,
      out SequencePosition consumed)
  {
    message = default;
    consumed = buffer.Start;
    if (buffer.Length < 5)
    {
      return false;
    }

    Span<byte> header = stackalloc byte[5];
    buffer.Slice(0, 5).CopyTo(header);
    int length = BinaryPrimitives.ReadInt32BigEndian(header[1..]);
    if (length < 4)
    {
      throw new InvalidDataException($"Invalid PostgreSQL message length {length}.");
    }

    if (length > MaximumMessageLength)
    {
      throw new InvalidDataException(
        $"PostgreSQL message length {length} exceeds {MaximumMessageLength} bytes.");
    }

    long totalLength = 1L + length;
    if (buffer.Length < totalLength)
    {
      return false;
    }

    int payloadLength = length - 4;
    byte[]? payload = payloadLength == 0
      ? null
      : ArrayPool<byte>.Shared.Rent(payloadLength);
    if (payload is not null)
    {
      buffer.Slice(5, payloadLength).CopyTo(payload);
    }

    consumed = buffer.GetPosition(totalLength);
    message = new PgWireMessage(header[0], payload, payloadLength);
    return true;
  }
}

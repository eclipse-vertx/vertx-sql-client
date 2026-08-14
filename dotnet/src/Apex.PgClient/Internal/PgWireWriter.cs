/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Buffers;
using System.Buffers.Binary;
using System.IO.Pipelines;
using System.Security.Cryptography;
using System.Text;
using Apex.SqlClient;

namespace Apex.PgClient.Internal;

internal sealed class PgWireWriter
{
  private static readonly Encoding Utf8 = new UTF8Encoding(false, true);
  private readonly PipeWriter _writer;

  public PgWireWriter(PipeWriter writer)
  {
    _writer = writer;
  }

  public ValueTask<FlushResult> WriteStartupAsync(
      PgConnectOptions options,
      CancellationToken cancellationToken)
  {
    ArrayBufferWriter<byte> payload = new();
    payload.WriteInt32(196608);
    payload.WriteCString("user");
    payload.WriteCString(options.Username);
    payload.WriteCString("database");
    payload.WriteCString(options.Database);
    foreach ((string key, string value) in options.Properties)
    {
      payload.WriteCString(key);
      payload.WriteCString(value);
    }

    payload.WriteByte(0);
    return WriteUntypedAsync(payload.WrittenMemory, cancellationToken);
  }

  public ValueTask<FlushResult> WritePasswordAsync(
      string password,
      CancellationToken cancellationToken)
  {
    ArrayBufferWriter<byte> payload = new();
    payload.WriteCString(password);
    return WriteTypedAsync((byte)'p', payload.WrittenMemory, cancellationToken);
  }

  public ValueTask<FlushResult> WriteSaslInitialAsync(
      string mechanism,
      string message,
      CancellationToken cancellationToken)
  {
    ArrayBufferWriter<byte> payload = new();
    payload.WriteCString(mechanism);
    int byteCount = Utf8.GetByteCount(message);
    payload.WriteInt32(byteCount);
    payload.WriteUtf8(message);
    return WriteTypedAsync((byte)'p', payload.WrittenMemory, cancellationToken);
  }

  public ValueTask<FlushResult> WriteSaslResponseAsync(
      string message,
      CancellationToken cancellationToken)
  {
    ArrayBufferWriter<byte> payload = new();
    payload.WriteUtf8(message);
    return WriteTypedAsync((byte)'p', payload.WrittenMemory, cancellationToken);
  }

  public async ValueTask<FlushResult> WriteQueryAsync(
    string sql,
    CancellationToken cancellationToken)
  {
    int byteCount = Utf8.GetByteCount(sql);
    int totalLength = 1 + sizeof(int) + byteCount + 1;
    Span<byte> message = _writer.GetSpan(totalLength);
    message[0] = (byte)'Q';
    BinaryPrimitives.WriteInt32BigEndian(
      message[1..],
      sizeof(int) + byteCount + 1);
    int written = Utf8.GetBytes(sql, message[5..]);
    message[5 + written] = 0;
    _writer.Advance(totalLength);
    return await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask WriteExtendedQueryAsync(
      string sql,
      SqlParameters parameters,
      CancellationToken cancellationToken)
  {
    ArrayBufferWriter<byte> parse = new();
    parse.WriteByte(0);
    parse.WriteCString(sql);
    parse.WriteInt16(0);
    WriteTyped((byte)'P', parse.WrittenSpan);

    WriteBindDescribeExecute(string.Empty, string.Empty, parameters, 0);
    await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask WritePrepareAsync(
      string name,
      string sql,
      CancellationToken cancellationToken)
  {
    ArrayBufferWriter<byte> parse = new();
    parse.WriteCString(name);
    parse.WriteCString(sql);
    parse.WriteInt16(0);
    WriteTyped((byte)'P', parse.WrittenSpan);
    WriteTyped((byte)'S', ReadOnlySpan<byte>.Empty);
    await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask WritePreparedQueryAsync(
      string name,
      SqlParameters parameters,
      CancellationToken cancellationToken)
  {
    WriteBindDescribeExecute(string.Empty, name, parameters, 0);
    await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask WriteOpenPortalAsync(
    string portalName,
    string statementName,
    SqlParameters parameters,
    int fetchSize,
    CancellationToken cancellationToken)
  {
    WriteBindDescribeExecute(portalName, statementName, parameters, fetchSize);
    await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask WriteExecutePortalAsync(
    string portalName,
    int fetchSize,
    CancellationToken cancellationToken)
  {
    WriteExecute(portalName, fetchSize);
    WriteTyped((byte)'S', ReadOnlySpan<byte>.Empty);
    await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask WriteClosePortalAsync(
    string portalName,
    CancellationToken cancellationToken)
  {
    ArrayBufferWriter<byte> close = new();
    close.WriteByte((byte)'P');
    close.WriteCString(portalName);
    WriteTyped((byte)'C', close.WrittenSpan);
    WriteTyped((byte)'S', ReadOnlySpan<byte>.Empty);
    await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  public async ValueTask WriteCloseStatementAsync(
      string name,
      CancellationToken cancellationToken)
  {
    ArrayBufferWriter<byte> close = new();
    close.WriteByte((byte)'S');
    close.WriteCString(name);
    WriteTyped((byte)'C', close.WrittenSpan);
    WriteTyped((byte)'S', ReadOnlySpan<byte>.Empty);
    await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  public ValueTask<FlushResult> WriteTerminateAsync(CancellationToken cancellationToken) =>
      WriteTypedAsync((byte)'X', ReadOnlyMemory<byte>.Empty, cancellationToken);

  public static string Md5Password(string password, string username, ReadOnlySpan<byte> salt)
  {
    byte[] firstInput = Utf8.GetBytes(password + username);
    Span<byte> firstHash = stackalloc byte[MD5.HashSizeInBytes];
    MD5.HashData(firstInput, firstHash);

    string firstHex = Convert.ToHexStringLower(firstHash);
    byte[] secondPrefix = Utf8.GetBytes(firstHex);
    byte[] secondInput = GC.AllocateUninitializedArray<byte>(secondPrefix.Length + salt.Length);
    secondPrefix.CopyTo(secondInput, 0);
    salt.CopyTo(secondInput.AsSpan(secondPrefix.Length));

    Span<byte> secondHash = stackalloc byte[MD5.HashSizeInBytes];
    MD5.HashData(secondInput, secondHash);
    return "md5" + Convert.ToHexStringLower(secondHash);
  }

  private async ValueTask<FlushResult> WriteUntypedAsync(
      ReadOnlyMemory<byte> payload,
      CancellationToken cancellationToken)
  {
    Span<byte> length = _writer.GetSpan(sizeof(int));
    BinaryPrimitives.WriteInt32BigEndian(length, checked(payload.Length + sizeof(int)));
    _writer.Advance(sizeof(int));
    _writer.Write(payload.Span);
    return await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  private async ValueTask<FlushResult> WriteTypedAsync(
      byte type,
      ReadOnlyMemory<byte> payload,
      CancellationToken cancellationToken)
  {
    WriteTyped(type, payload.Span);
    return await _writer.FlushAsync(cancellationToken).ConfigureAwait(false);
  }

  private void WriteTyped(byte type, ReadOnlySpan<byte> payload)
  {
    Span<byte> header = _writer.GetSpan(5);
    header[0] = type;
    BinaryPrimitives.WriteInt32BigEndian(header[1..], checked(payload.Length + sizeof(int)));
    _writer.Advance(5);
    _writer.Write(payload);
  }

  private void WriteBindDescribeExecute(
    string portalName,
    string statementName,
    SqlParameters parameters,
    int fetchSize)
  {
    ArrayBufferWriter<byte> bind = new();
    bind.WriteCString(portalName);
    bind.WriteCString(statementName);
    bind.WriteInt16(0);
    bind.WriteInt16(checked((short)parameters.Count));
    for (int i = 0; i < parameters.Count; i++)
    {
      SqlValue value = parameters[i];
      if (value.IsNull)
      {
        bind.WriteInt32(-1);
        continue;
      }

      string text = PgTextCodec.FormatParameter(value);
      int byteCount = Utf8.GetByteCount(text);
      bind.WriteInt32(byteCount);
      bind.WriteUtf8(text);
    }

    bind.WriteInt16(1);
    bind.WriteInt16(1);
    WriteTyped((byte)'B', bind.WrittenSpan);

    ArrayBufferWriter<byte> describe = new();
    describe.WriteByte((byte)'P');
    describe.WriteCString(portalName);
    WriteTyped((byte)'D', describe.WrittenSpan);
    WriteExecute(portalName, fetchSize);
    WriteTyped((byte)'S', ReadOnlySpan<byte>.Empty);
  }

  private void WriteExecute(string portalName, int fetchSize)
  {
    ArrayBufferWriter<byte> execute = new();
    execute.WriteCString(portalName);
    execute.WriteInt32(fetchSize);
    WriteTyped((byte)'E', execute.WrittenSpan);
  }
}

internal static class PgBufferWriterExtensions
{
  private static readonly Encoding Utf8 = new UTF8Encoding(false, true);

  public static void WriteByte(this IBufferWriter<byte> writer, byte value)
  {
    writer.GetSpan(1)[0] = value;
    writer.Advance(1);
  }

  public static void WriteInt16(this IBufferWriter<byte> writer, short value)
  {
    Span<byte> destination = writer.GetSpan(sizeof(short));
    BinaryPrimitives.WriteInt16BigEndian(destination, value);
    writer.Advance(sizeof(short));
  }

  public static void WriteInt32(this IBufferWriter<byte> writer, int value)
  {
    Span<byte> destination = writer.GetSpan(sizeof(int));
    BinaryPrimitives.WriteInt32BigEndian(destination, value);
    writer.Advance(sizeof(int));
  }

  public static void WriteCString(this IBufferWriter<byte> writer, string value)
  {
    writer.WriteUtf8(value);
    writer.WriteByte(0);
  }

  public static void WriteUtf8(this IBufferWriter<byte> writer, string value)
  {
    int byteCount = Utf8.GetByteCount(value);
    Span<byte> destination = writer.GetSpan(byteCount);
    int written = Utf8.GetBytes(value, destination);
    writer.Advance(written);
  }
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Text.Json;

namespace Apex.SqlClient;

/// <summary>A typed SQL parameter value that avoids boxing common scalar values.</summary>
public readonly struct SqlValue
{
  private readonly long _scalar;
  private readonly decimal _decimal;
  private readonly object? _reference;

  private SqlValue(SqlValueKind kind, long scalar = 0, decimal decimalValue = 0, object? reference = null)
  {
    Kind = kind;
    _scalar = scalar;
    _decimal = decimalValue;
    _reference = reference;
  }

  public static SqlValue Null => default;

  public SqlValueKind Kind { get; }

  public bool IsNull => Kind == SqlValueKind.Null;

  public static SqlValue From(object? value) =>
    value switch
    {
      null => Null,
      SqlValue sqlValue => sqlValue,
      bool typed => typed,
      short typed => typed,
      int typed => typed,
      long typed => typed,
      float typed => typed,
      double typed => typed,
      decimal typed => typed,
      string typed => typed,
      byte[] typed => typed,
      ReadOnlyMemory<byte> typed => typed,
      Guid typed => typed,
      DateOnly typed => typed,
      TimeOnly typed => typed,
      DateTime typed => typed,
      DateTimeOffset typed => typed,
      JsonDocument typed => typed,
      JsonElement typed => typed,
      _ => new SqlValue(SqlValueKind.Object, reference: value),
    };

  public T? Get<T>()
  {
    object? value = ToObject();
    if (value is null)
    {
      return default;
    }

    return value is T typed
      ? typed
      : throw new InvalidCastException(
        $"SQL value contains {value.GetType().FullName}, not {typeof(T).FullName}.");
  }

  public T GetRequired<T>()
  {
    object? value = ToObject();
    return value is T typed
      ? typed
      : throw new InvalidCastException(
        value is null
          ? "SQL value contains NULL."
          : $"SQL value contains {value.GetType().FullName}, not {typeof(T).FullName}.");
  }

  public object? ToObject() =>
    Kind switch
    {
      SqlValueKind.Null => null,
      SqlValueKind.Boolean => _scalar != 0,
      SqlValueKind.Int16 => (short)_scalar,
      SqlValueKind.Int32 => (int)_scalar,
      SqlValueKind.Int64 => _scalar,
      SqlValueKind.Single => BitConverter.Int32BitsToSingle((int)_scalar),
      SqlValueKind.Double => BitConverter.Int64BitsToDouble(_scalar),
      SqlValueKind.Decimal => _decimal,
      SqlValueKind.DateOnly => DateOnly.FromDayNumber((int)_scalar),
      SqlValueKind.TimeOnly => new TimeOnly(_scalar),
      SqlValueKind.DateTime => DateTime.FromBinary(_scalar),
      _ => _reference,
    };

  public static implicit operator SqlValue(bool value) =>
    new(SqlValueKind.Boolean, value ? 1 : 0);

  public static implicit operator SqlValue(short value) =>
    new(SqlValueKind.Int16, value);

  public static implicit operator SqlValue(int value) =>
    new(SqlValueKind.Int32, value);

  public static implicit operator SqlValue(long value) =>
    new(SqlValueKind.Int64, value);

  public static implicit operator SqlValue(float value) =>
    new(SqlValueKind.Single, BitConverter.SingleToInt32Bits(value));

  public static implicit operator SqlValue(double value) =>
    new(SqlValueKind.Double, BitConverter.DoubleToInt64Bits(value));

  public static implicit operator SqlValue(decimal value) =>
    new(SqlValueKind.Decimal, decimalValue: value);

  public static implicit operator SqlValue(string value) =>
    new(SqlValueKind.String, reference: value);

  public static implicit operator SqlValue(byte[] value) =>
    new(SqlValueKind.Bytes, reference: value);

  public static implicit operator SqlValue(ReadOnlyMemory<byte> value) =>
    new(SqlValueKind.ReadOnlyMemory, reference: value);

  public static implicit operator SqlValue(Guid value) =>
    new(SqlValueKind.Guid, reference: value);

  public static implicit operator SqlValue(DateOnly value) =>
    new(SqlValueKind.DateOnly, value.DayNumber);

  public static implicit operator SqlValue(TimeOnly value) =>
    new(SqlValueKind.TimeOnly, value.Ticks);

  public static implicit operator SqlValue(DateTime value) =>
    new(SqlValueKind.DateTime, value.ToBinary());

  public static implicit operator SqlValue(DateTimeOffset value) =>
    new(SqlValueKind.DateTimeOffset, reference: value);

  public static implicit operator SqlValue(JsonDocument value) =>
    new(SqlValueKind.JsonDocument, reference: value);

  public static implicit operator SqlValue(JsonElement value) =>
    new(SqlValueKind.JsonElement, reference: value);
}

public enum SqlValueKind : byte
{
  Null,
  Boolean,
  Int16,
  Int32,
  Int64,
  Single,
  Double,
  Decimal,
  String,
  Bytes,
  ReadOnlyMemory,
  Guid,
  DateOnly,
  TimeOnly,
  DateTime,
  DateTimeOffset,
  JsonDocument,
  JsonElement,
  Object,
}

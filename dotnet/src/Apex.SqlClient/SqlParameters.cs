/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Collections;

namespace Apex.SqlClient;

/// <summary>An immutable ordered set of SQL parameter values.</summary>
public readonly struct SqlParameters : IReadOnlyList<SqlValue>
{
  private readonly SqlValue[]? _values;

  public static SqlParameters Empty => default;

  public int Count => _values?.Length ?? 0;

  public SqlValue this[int index] =>
      _values is null ? throw new ArgumentOutOfRangeException(nameof(index)) : _values[index];

  public static SqlParameters Create(params SqlValue[] values)
  {
    ArgumentNullException.ThrowIfNull(values);
    return values.Length == 0 ? Empty : new SqlParameters((SqlValue[])values.Clone());
  }

  public static SqlParameters From(ReadOnlySpan<SqlValue> values) =>
      values.IsEmpty ? Empty : new SqlParameters(values.ToArray());

  public static SqlParameters FromObjects(ReadOnlySpan<object?> values)
  {
    if (values.IsEmpty)
    {
      return Empty;
    }

    SqlValue[] converted = new SqlValue[values.Length];
    for (int i = 0; i < values.Length; i++)
    {
      converted[i] = SqlValue.From(values[i]);
    }

    return new SqlParameters(converted);
  }

  internal ReadOnlySpan<SqlValue> Span => _values;

  private SqlParameters(SqlValue[] values)
  {
    _values = values;
  }

  public IEnumerator<SqlValue> GetEnumerator() =>
      ((IEnumerable<SqlValue>)(_values ?? Array.Empty<SqlValue>())).GetEnumerator();

  IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();
}

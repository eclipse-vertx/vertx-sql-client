/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Diagnostics.CodeAnalysis;

namespace Apex.SqlClient;

/// <summary>An immutable materialized database row.</summary>
public sealed class SqlRow
{
  private readonly IReadOnlyList<SqlColumn> _columns;
  private readonly object?[] _values;

  internal SqlRow(IReadOnlyList<SqlColumn> columns, object?[] values)
  {
    if (columns.Count != values.Length)
    {
      throw new ArgumentException("Column and value counts must match.", nameof(values));
    }

    _columns = columns;
    _values = values;
  }

  public int Count => _values.Length;

  public object? this[int ordinal] => _values[ordinal];

  public object? this[string name] => _values[GetOrdinal(name)];

  public bool IsNull(int ordinal) => _values[ordinal] is null;

  public int GetOrdinal(string name)
  {
    ArgumentException.ThrowIfNullOrEmpty(name);
    int hash = StringComparer.Ordinal.GetHashCode(name);
    for (int i = 0; i < _columns.Count; i++)
    {
      string candidate = _columns[i].Name;
      if (StringComparer.Ordinal.GetHashCode(candidate) == hash &&
          string.Equals(candidate, name, StringComparison.Ordinal))
      {
        return i;
      }
    }

    throw new IndexOutOfRangeException($"Column '{name}' does not exist.");
  }

  public T Get<T>(int ordinal)
  {
    object? value = _values[ordinal];
    if (value is null)
    {
      if (default(T) is null)
      {
        return default!;
      }

      throw new InvalidCastException($"Column {ordinal} contains NULL.");
    }

    if (value is T typed)
    {
      return typed;
    }

    throw new InvalidCastException(
        $"Column {ordinal} contains {value.GetType().FullName}, not {typeof(T).FullName}.");
  }

  public T Get<T>(string name) => Get<T>(GetOrdinal(name));

  public bool TryGet<T>(int ordinal, [MaybeNullWhen(false)] out T value)
  {
    if (_values[ordinal] is T typed)
    {
      value = typed;
      return true;
    }

    value = default;
    return false;
  }
}

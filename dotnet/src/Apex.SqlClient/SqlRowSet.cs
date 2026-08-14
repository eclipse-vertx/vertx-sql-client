/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Collections;

namespace Apex.SqlClient;

/// <summary>A buffered query result that remains valid for its managed lifetime.</summary>
public sealed class SqlRowSet : IReadOnlyList<SqlRow>
{
  private readonly SqlRow[] _rows;

  internal SqlRowSet(
      IReadOnlyList<SqlColumn> columns,
      SqlRow[] rows,
      long affectedRows,
      string commandTag,
      SqlRowSet? next = null)
  {
    Columns = columns;
    _rows = rows;
    AffectedRows = affectedRows;
    CommandTag = commandTag;
    Next = next;
  }

  public static SqlRowSet Empty { get; } =
      new(Array.Empty<SqlColumn>(), Array.Empty<SqlRow>(), 0, string.Empty);

  public IReadOnlyList<SqlColumn> Columns { get; }

  public long AffectedRows { get; }

  public string CommandTag { get; }

  public SqlRowSet? Next { get; }

  public int Count => _rows.Length;

  public SqlRow this[int index] => _rows[index];

  public IEnumerator<SqlRow> GetEnumerator() => ((IEnumerable<SqlRow>)_rows).GetEnumerator();

  IEnumerator IEnumerable.GetEnumerator() => _rows.GetEnumerator();
}

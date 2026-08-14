/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;

namespace Apex.PgClient;

public sealed class PgException : SqlClientException
{
  internal PgException(IReadOnlyDictionary<char, string> fields)
      : base(fields.TryGetValue('M', out string? message) ? message : "PostgreSQL error")
  {
    Severity = Get(fields, 'V') ?? Get(fields, 'S');
    SqlState = Get(fields, 'C');
    Detail = Get(fields, 'D');
    Hint = Get(fields, 'H');
    SchemaName = Get(fields, 's');
    TableName = Get(fields, 't');
    ColumnName = Get(fields, 'c');
    ConstraintName = Get(fields, 'n');
    Position = int.TryParse(Get(fields, 'P'), out int position) ? position : null;
  }

  public string? Severity { get; }

  public string? SqlState { get; }

  public string? Detail { get; }

  public string? Hint { get; }

  public int? Position { get; }

  public string? SchemaName { get; }

  public string? TableName { get; }

  public string? ColumnName { get; }

  public string? ConstraintName { get; }

  private static string? Get(IReadOnlyDictionary<char, string> fields, char key) =>
      fields.TryGetValue(key, out string? value) ? value : null;
}

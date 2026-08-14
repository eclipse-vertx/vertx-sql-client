/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.PgClient.Internal;

internal static class PgArrayParser
{
  public static string?[] Parse(string value, char delimiter = ',')
  {
    ReadOnlySpan<char> input = value.AsSpan();
    int dimensions = input.IndexOf('=');
    if (dimensions >= 0)
    {
      input = input[(dimensions + 1)..];
    }

    if (input.Length < 2 || input[0] != '{' || input[^1] != '}')
    {
      throw new FormatException("Invalid PostgreSQL array.");
    }

    input = input[1..^1];
    if (input.IsEmpty)
    {
      return [];
    }

    List<string?> values = [];
    int position = 0;
    while (position < input.Length)
    {
      if (input[position] == '{')
      {
        throw new NotSupportedException("Multidimensional PostgreSQL arrays are not supported yet.");
      }

      bool quoted = input[position] == '"';
      if (quoted)
      {
        position++;
      }

      System.Text.StringBuilder item = new();
      bool closed = !quoted;
      while (position < input.Length)
      {
        char current = input[position++];
        if (current == '\\')
        {
          if (position == input.Length)
          {
            throw new FormatException("PostgreSQL array has a trailing escape.");
          }

          item.Append(input[position++]);
          continue;
        }

        if (quoted && current == '"')
        {
          closed = true;
          break;
        }

        if (!quoted && current == delimiter)
        {
          position--;
          break;
        }

        item.Append(current);
      }

      if (!closed)
      {
        throw new FormatException("PostgreSQL array has an unterminated quoted value.");
      }

      string parsed = item.ToString();
      values.Add(!quoted && parsed == "NULL" ? null : parsed);
      if (position < input.Length)
      {
        if (input[position++] != delimiter)
        {
          throw new FormatException("PostgreSQL array values must be comma-separated.");
        }
      }
    }

    return values.ToArray();
  }
}

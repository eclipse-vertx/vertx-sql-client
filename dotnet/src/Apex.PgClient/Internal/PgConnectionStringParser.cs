/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.PgClient.Internal;

internal static class PgConnectionStringParser
{
  public static IReadOnlyDictionary<string, string> ParseKeywords(string connectionString)
  {
    Dictionary<string, string> values = new(StringComparer.OrdinalIgnoreCase);
    ReadOnlySpan<char> input = connectionString.AsSpan();
    int position = 0;
    while (position < input.Length)
    {
      SkipWhitespace(input, ref position);
      if (position == input.Length)
      {
        break;
      }

      int keyStart = position;
      while (position < input.Length && input[position] != '=' && !char.IsWhiteSpace(input[position]))
      {
        position++;
      }

      if (position == keyStart)
      {
        throw new FormatException("PostgreSQL connection-string key is empty.");
      }

      string key = input[keyStart..position].ToString();
      SkipWhitespace(input, ref position);
      if (position == input.Length || input[position++] != '=')
      {
        throw new FormatException($"PostgreSQL connection-string key '{key}' has no value.");
      }

      SkipWhitespace(input, ref position);
      string value = position < input.Length && input[position] == '\''
        ? ParseQuoted(input, ref position)
        : ParseUnquoted(input, ref position);
      values[key] = value;
    }

    return values;
  }

  public static IReadOnlyDictionary<string, string> ParseQuery(string query)
  {
    Dictionary<string, string> values = new(StringComparer.OrdinalIgnoreCase);
    foreach (string part in query.TrimStart('?').Split('&', StringSplitOptions.RemoveEmptyEntries))
    {
      int separator = part.IndexOf('=');
      string key = separator < 0 ? part : part[..separator];
      string value = separator < 0 ? string.Empty : part[(separator + 1)..];
      values[Decode(key)] = Decode(value);
    }

    return values;
  }

  private static string ParseQuoted(ReadOnlySpan<char> input, ref int position)
  {
    position++;
    System.Text.StringBuilder value = new();
    while (position < input.Length)
    {
      char current = input[position++];
      if (current == '\'')
      {
        return value.ToString();
      }

      if (current == '\\')
      {
        if (position == input.Length)
        {
          throw new FormatException("PostgreSQL quoted connection-string value has a trailing escape.");
        }

        current = input[position++];
      }

      value.Append(current);
    }

    throw new FormatException("PostgreSQL quoted connection-string value is unterminated.");
  }

  private static string ParseUnquoted(ReadOnlySpan<char> input, ref int position)
  {
    System.Text.StringBuilder value = new();
    while (position < input.Length && !char.IsWhiteSpace(input[position]))
    {
      char current = input[position++];
      if (current == '\\')
      {
        if (position == input.Length)
        {
          throw new FormatException("PostgreSQL connection-string value has a trailing escape.");
        }

        current = input[position++];
      }

      value.Append(current);
    }

    return value.ToString();
  }

  private static void SkipWhitespace(ReadOnlySpan<char> input, ref int position)
  {
    while (position < input.Length && char.IsWhiteSpace(input[position]))
    {
      position++;
    }
  }

  private static string Decode(string value) =>
    Uri.UnescapeDataString(value.Replace("+", " ", StringComparison.Ordinal));
}

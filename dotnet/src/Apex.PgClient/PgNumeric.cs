/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Globalization;
using System.Numerics;

namespace Apex.PgClient;

public readonly record struct PgNumeric
{
  private PgNumeric(BigInteger unscaledValue, int scale, PgNumericSpecialValue specialValue)
  {
    UnscaledValue = unscaledValue;
    Scale = scale;
    SpecialValue = specialValue;
  }

  public BigInteger UnscaledValue { get; }

  public int Scale { get; }

  public PgNumericSpecialValue SpecialValue { get; }

  public bool IsFinite => SpecialValue == PgNumericSpecialValue.Finite;

  public static PgNumeric NaN { get; } =
    new(BigInteger.Zero, 0, PgNumericSpecialValue.NaN);

  public static PgNumeric PositiveInfinity { get; } =
    new(BigInteger.Zero, 0, PgNumericSpecialValue.PositiveInfinity);

  public static PgNumeric NegativeInfinity { get; } =
    new(BigInteger.Zero, 0, PgNumericSpecialValue.NegativeInfinity);

  public static PgNumeric Create(BigInteger unscaledValue, int scale)
  {
    ArgumentOutOfRangeException.ThrowIfNegative(scale);
    return new PgNumeric(unscaledValue, scale, PgNumericSpecialValue.Finite);
  }

  public static PgNumeric Parse(string value)
  {
    ArgumentException.ThrowIfNullOrWhiteSpace(value);
    if (value.Equals("NaN", StringComparison.OrdinalIgnoreCase))
    {
      return NaN;
    }

    if (value.Equals("Infinity", StringComparison.OrdinalIgnoreCase))
    {
      return PositiveInfinity;
    }

    if (value.Equals("-Infinity", StringComparison.OrdinalIgnoreCase))
    {
      return NegativeInfinity;
    }

    ReadOnlySpan<char> text = value.AsSpan();
    int exponentIndex = text.IndexOfAny('e', 'E');
    int exponent = exponentIndex < 0
      ? 0
      : int.Parse(text[(exponentIndex + 1)..], NumberStyles.Integer, CultureInfo.InvariantCulture);
    ReadOnlySpan<char> significand = exponentIndex < 0 ? text : text[..exponentIndex];
    int decimalIndex = significand.IndexOf('.');
    int fractionalDigits = decimalIndex < 0 ? 0 : significand.Length - decimalIndex - 1;
    string digits = decimalIndex < 0
      ? significand.ToString()
      : string.Concat(significand[..decimalIndex], significand[(decimalIndex + 1)..]);
    BigInteger unscaled = BigInteger.Parse(digits, NumberStyles.Integer, CultureInfo.InvariantCulture);
    int scale = fractionalDigits - exponent;
    if (scale < 0)
    {
      unscaled *= BigInteger.Pow(10, -scale);
      scale = 0;
    }

    return new PgNumeric(unscaled, scale, PgNumericSpecialValue.Finite);
  }

  public decimal ToDecimal() =>
    IsFinite
      ? decimal.Parse(ToString(), NumberStyles.Number, CultureInfo.InvariantCulture)
      : throw new OverflowException("A non-finite PostgreSQL numeric cannot convert to decimal.");

  public override string ToString()
  {
    if (!IsFinite)
    {
      return SpecialValue switch
      {
        PgNumericSpecialValue.NaN => "NaN",
        PgNumericSpecialValue.PositiveInfinity => "Infinity",
        _ => "-Infinity",
      };
    }

    bool negative = UnscaledValue.Sign < 0;
    string digits = BigInteger.Abs(UnscaledValue).ToString(CultureInfo.InvariantCulture);
    if (Scale == 0)
    {
      return negative ? "-" + digits : digits;
    }

    if (digits.Length <= Scale)
    {
      digits = new string('0', Scale - digits.Length + 1) + digits;
    }

    int decimalPosition = digits.Length - Scale;
    string formatted = digits.Insert(decimalPosition, ".");
    return negative ? "-" + formatted : formatted;
  }
}

public enum PgNumericSpecialValue : byte
{
  Finite,
  NaN,
  PositiveInfinity,
  NegativeInfinity,
}

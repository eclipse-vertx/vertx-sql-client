/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using System.Net;

namespace Apex.PgClient;

public readonly record struct PgInterval(
  int Years,
  int Months,
  int Days,
  int Hours,
  int Minutes,
  int Seconds,
  int Microseconds);

public readonly record struct PgPoint(double X, double Y);

public readonly record struct PgLine(double A, double B, double C);

public readonly record struct PgLineSegment(PgPoint Start, PgPoint End);

public readonly record struct PgBox(PgPoint UpperRight, PgPoint LowerLeft);

public sealed record PgPath(IReadOnlyList<PgPoint> Points, bool Closed);

public sealed record PgPolygon(IReadOnlyList<PgPoint> Points);

public readonly record struct PgCircle(PgPoint Center, double Radius);

public readonly record struct PgTimeWithTimeZone(TimeOnly Time, TimeSpan Offset);

public readonly record struct PgMoney
{
  public PgMoney(decimal value)
  {
    decimal normalized = decimal.Parse(
      value.ToString("0.##", System.Globalization.CultureInfo.InvariantCulture),
      System.Globalization.CultureInfo.InvariantCulture);
    if (normalized != value)
    {
      throw new ArgumentOutOfRangeException(nameof(value), "Money supports at most two fractional digits.");
    }

    Value = value;
  }

  public decimal Value { get; }
}

public readonly record struct PgInet
{
  public PgInet(IPAddress address, int? prefixLength = null)
  {
    ArgumentNullException.ThrowIfNull(address);
    ValidatePrefix(address, prefixLength);
    Address = address;
    PrefixLength = prefixLength;
  }

  public IPAddress Address { get; }

  public int? PrefixLength { get; }

  internal static void ValidatePrefix(IPAddress address, int? prefixLength)
  {
    int maximum = address.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork ? 32 : 128;
    if (prefixLength is < 0 || prefixLength > maximum)
    {
      throw new ArgumentOutOfRangeException(nameof(prefixLength));
    }
  }
}

public readonly record struct PgCidr
{
  public PgCidr(IPAddress address, int prefixLength)
  {
    ArgumentNullException.ThrowIfNull(address);
    PgInet.ValidatePrefix(address, prefixLength);
    Address = address;
    PrefixLength = prefixLength;
  }

  public IPAddress Address { get; }

  public int PrefixLength { get; }
}

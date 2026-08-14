/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

/// <summary>Describes one result column.</summary>
public sealed record SqlColumn(
    string Name,
    uint TypeId,
    short TypeSize,
    int TypeModifier,
    SqlDataFormat Format);

public enum SqlDataFormat : short
{
  Text = 0,
  Binary = 1,
}

/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.SqlClient;

namespace Apex.PgClient;

public sealed class PgUnsupportedTypeException : SqlClientException
{
  public PgUnsupportedTypeException(uint typeId)
    : base($"PostgreSQL type OID {typeId} is not supported.")
  {
    TypeId = typeId;
  }

  public uint TypeId { get; }
}

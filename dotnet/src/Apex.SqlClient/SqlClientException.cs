/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

public class SqlClientException : Exception
{
  public SqlClientException(string message)
      : base(message)
  {
  }

  public SqlClientException(string message, Exception innerException)
      : base(message, innerException)
  {
  }
}

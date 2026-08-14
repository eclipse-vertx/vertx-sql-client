/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

using Apex.PgClient;
using Apex.SqlClient;

PgConnectOptions options = PgConnectOptions.Parse(
  "host=localhost port=5432 user=user password=pass dbname=db sslmode=disable");
SqlParameters parameters = SqlParameters.Create(42, "value", DateOnly.FromDateTime(DateTime.UtcNow));
Console.WriteLine($"{options.Host}:{options.Port}/{options.Database} parameters={parameters.Count}");

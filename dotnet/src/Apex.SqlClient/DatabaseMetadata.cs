/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

public sealed record DatabaseMetadata(
    string ProductName,
    string FullVersion,
    int MajorVersion,
    int MinorVersion);

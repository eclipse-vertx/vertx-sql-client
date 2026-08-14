/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.SqlClient;

/// <summary>The outcome of a command that does not require a buffered row result.</summary>
public readonly record struct SqlCommandResult(long AffectedRows, string CommandTag);

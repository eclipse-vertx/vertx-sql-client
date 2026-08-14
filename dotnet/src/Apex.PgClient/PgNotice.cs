/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

namespace Apex.PgClient;

public sealed record PgNotice(
    string Message,
    string? Severity,
    string? SqlState,
    string? Detail,
    string? Hint);

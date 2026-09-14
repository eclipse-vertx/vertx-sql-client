/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient.impl;

import io.vertx.pgclient.PgCopyOutOptions;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.SqlCommand;

public final class CopyOutStreamCommand extends CommandBase<Void> implements SqlCommand {

  private final String sql;
  private final CopyOutStreamImpl out;
  private final PgCopyOutOptions options;

  public CopyOutStreamCommand(String sql, CopyOutStreamImpl out, PgCopyOutOptions options) {
    this.sql = sql;
    this.out = out;
    this.options = options;
  }

  @Override
  public String sql() {
    return sql;
  }

  public CopyOutStreamImpl out() {
    return out;
  }

  public PgCopyOutOptions options() {
    return options;
  }
}

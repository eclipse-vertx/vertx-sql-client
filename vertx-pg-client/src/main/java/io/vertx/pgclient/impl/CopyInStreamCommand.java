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

import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.SqlCommand;

public final class CopyInStreamCommand extends CommandBase<Void> implements SqlCommand {
  private final String sql;
  private final CopyInStreamInternal in;

  public CopyInStreamCommand(String sql, CopyInStreamInternal in) {
    this.sql = sql;
    this.in = in;
  }

  @Override
  public String sql() {
    return sql;
  }

  public CopyInStreamInternal in() {
    return in;
  }
}

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

package io.vertx.sqlclient.spi.protocol;

/**
 * A probe command used to check a connection liveness without performing any
 * user-visible work.
 *
 * <p>The pool can schedule this command on idle pooled connections to detect stale
 * connections that have been silently dropped by the database or an intermediate
 * load balancer. A successful completion means the connection is still usable; a
 * failure means the connection should be evicted.</p>
 */
public class PingCommand extends CommandBase<Void> {

  private final String sql;

  /**
   * Create a keep-alive probe command that runs the given {@code sql} statement.
   *
   * @param sql the lightweight no-op statement used to verify liveness, e.g. {@code SELECT 1}
   */
  public PingCommand(String sql) {
    this.sql = sql;
  }

  /**
   * @return the keep-alive probe statement
   */
  public String sql() {
    return sql;
  }
}

/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.impl.command;

public class TxCommand extends CommandBase<Void> {

  public static final TxCommand BEGIN = new TxCommand("BEGIN");
  public static final TxCommand ROLLBACK = new TxCommand("ROLLBACK");
  public static final TxCommand COMMIT = new TxCommand("COMMIT");

  public final String sql;

  private TxCommand(String sql) {
    this.sql = sql;
  }
}

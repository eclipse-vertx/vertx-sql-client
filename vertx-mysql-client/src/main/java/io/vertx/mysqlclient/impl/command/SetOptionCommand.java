/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.impl.command;

import io.vertx.mysqlclient.MySQLSetOption;
import io.vertx.sqlclient.impl.command.CommandBase;

public class SetOptionCommand extends CommandBase<Void> {
  private final MySQLSetOption mySQLSetOption;

  public SetOptionCommand(MySQLSetOption mySQLSetOption) {
    this.mySQLSetOption = mySQLSetOption;
  }

  public MySQLSetOption option() {
    return mySQLSetOption;
  }
}

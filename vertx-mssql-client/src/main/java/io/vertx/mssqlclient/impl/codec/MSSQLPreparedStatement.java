/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.internal.RowDescriptorBase;

public class MSSQLPreparedStatement implements PreparedStatement {

  final String sql;
  int handle;

  public MSSQLPreparedStatement(String sql) {
    this.sql = sql;
  }

  @Override
  public RowDescriptorBase rowDesc() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String sql() {
    return sql;
  }

}

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

package io.vertx.mssqlclient.impl.codec;

import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.TupleInternal;

public class MSSQLPreparedStatement implements PreparedStatement {
  final String sql;
  final MSSQLParamDesc paramDesc;
  final boolean cacheable;

  public MSSQLPreparedStatement(String sql, MSSQLParamDesc paramDesc, boolean cacheable) {
    this.sql = sql;
    this.paramDesc = paramDesc;
    this.cacheable = cacheable;
  }

  @Override
  public ParamDesc paramDesc() {
    return paramDesc;
  }

  @Override
  public RowDesc rowDesc() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public String prepare(TupleInternal values) {
//    return paramDesc.prepare(values);
    return null;
  }

  @Override
  public boolean cacheable() {
    return cacheable;
  }
}

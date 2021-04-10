/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevsky
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeRowDesc;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.TupleInternal;

import java.util.Map;
import java.util.UUID;

public class ClickhouseNativePreparedStatement implements PreparedStatement {
  private final String sql;
  private final ClickhouseNativeParamDesc paramDesc;
  private final ClickhouseNativeRowDesc rowDesc;
  private final QueryInfo queryInfo;
  private final boolean sentQuery;
  private final UUID psId;

  public ClickhouseNativePreparedStatement(String sql, ClickhouseNativeParamDesc paramDesc, ClickhouseNativeRowDesc rowDesc,
                                           QueryInfo queryInfo, boolean sentQuery, UUID psId) {
    this.sql = sql;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
    this.queryInfo = queryInfo;
    this.sentQuery = sentQuery;
    this.psId = psId;
  }

  @Override
  public ParamDesc paramDesc() {
    return paramDesc;
  }

  @Override
  public RowDesc rowDesc() {
    return rowDesc;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public String prepare(TupleInternal values) {
    return null;
  }

  public QueryInfo queryInfo() {
    return queryInfo;
  }

  public boolean isSentQuery() {
    return sentQuery;
  }

  public UUID getPsId() {
    return psId;
  }
}

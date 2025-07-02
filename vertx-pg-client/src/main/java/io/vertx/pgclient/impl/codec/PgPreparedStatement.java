/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.pgclient.impl.codec;

import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.internal.TupleBase;

class PgPreparedStatement implements PreparedStatement {

  final String sql;
  final BindMessage bind;
  final PgParamDesc paramDesc;
  final PgRowDescriptor rowDesc;
  final boolean cached;

  PgPreparedStatement(String sql, byte[] statement, PgParamDesc paramDesc, PgRowDescriptor rowDesc, boolean cached) {
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
    this.sql = sql;
    this.bind = new BindMessage(statement, paramDesc != null ? paramDesc.paramDataTypes() : null, rowDesc != null ? rowDesc.columns : PgColumnDesc.EMPTY_COLUMNS);
    this.cached = cached;
  }

  @Override
  public PgRowDescriptor rowDesc() {
    return rowDesc;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public TupleBase prepare(TupleBase values) {
    return paramDesc.prepare(values);
  }

  public boolean isCached() {
    return cached;
  }

  @Override
  public String toString() {
    return "PreparedStatement[" + sql + "]";
  }
}

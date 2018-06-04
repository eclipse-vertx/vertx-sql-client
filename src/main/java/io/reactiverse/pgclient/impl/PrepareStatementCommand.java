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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgException;
import io.reactiverse.pgclient.impl.codec.TxStatus;
import io.reactiverse.pgclient.impl.codec.decoder.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.decoder.ParameterDescription;
import io.reactiverse.pgclient.impl.codec.decoder.RowDescription;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.reactiverse.pgclient.impl.codec.encoder.Describe;
import io.reactiverse.pgclient.impl.codec.encoder.Parse;
import io.vertx.core.Handler;

public class PrepareStatementCommand extends CommandBase<PreparedStatement> {

  final String sql;
  long statement; // 0 means unamed statement otherwise CString
  SocketConnection.CachedPreparedStatement cached;
  private ParameterDescription parameterDesc;
  private RowDescription rowDesc;

  PrepareStatementCommand(String sql, Handler<? super CommandResponse<PreparedStatement>> handler) {
    super(handler);
    this.sql = sql;
  }

  @Override
  void exec(MessageEncoder out) {
    out.writeParse(new Parse(sql, statement));
    out.writeDescribe(new Describe(statement, null));
    out.writeSync();
  }

  @Override
  public void handleParseComplete() {
    // Response to parse
  }

  @Override
  public void handleParameterDescription(ParameterDescription parameterDesc) {
    // Response to Describe
    this.parameterDesc = parameterDesc;
  }

  @Override
  public void handleRowDescription(RowDescription rowDesc) {
    // Response to Describe
    this.rowDesc = rowDesc;
  }

  @Override
  public void handleNoData() {
    // Response to Describe
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    failure = new PgException(errorResponse);
  }

  @Override
  public void handleReadyForQuery(TxStatus txStatus) {
    result = new PreparedStatement(sql, statement, parameterDesc, rowDesc);
    super.handleReadyForQuery(txStatus);
  }
}

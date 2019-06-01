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

import io.vertx.sqlclient.impl.TxStatus;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.sqlclient.impl.PreparedStatement;

class PrepareStatementCommandCodec extends PgCommandCodec<PreparedStatement, PrepareStatementCommand> {

  private PgParamDesc parameterDesc;
  private PgRowDesc rowDesc;

  PrepareStatementCommandCodec(PrepareStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(PgEncoder encoder) {
    encoder.writeParse(new Parse(cmd.sql(), cmd.statement()));
    encoder.writeDescribe(new Describe(cmd.statement(), null));
    encoder.writeSync();
  }

  @Override
  public void handleParseComplete() {
    // Response to parse
  }

  @Override
  public void handleParameterDescription(PgParamDesc paramDesc) {
    // Response to Describe
    this.parameterDesc = paramDesc;
  }

  @Override
  public void handleRowDescription(PgRowDesc rowDesc) {
    // Response to Describe
    this.rowDesc = rowDesc;
  }

  @Override
  public void handleNoData() {
    // Response to Describe
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    failure = errorResponse.toException();
  }

  @Override
  public void handleReadyForQuery(TxStatus txStatus) {
    result = new PgPreparedStatement(cmd.sql(), cmd.statement(), this.parameterDesc, this.rowDesc);
    super.handleReadyForQuery(txStatus);
  }
}

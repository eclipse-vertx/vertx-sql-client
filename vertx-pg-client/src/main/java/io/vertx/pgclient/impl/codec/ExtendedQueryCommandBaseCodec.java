/*
 * Copyright (C) 2018 Julien Viet
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

import io.vertx.sqlclient.impl.InvalidCachedStatementExecutionEvent;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommandBase;

abstract class ExtendedQueryCommandBaseCodec<R, C extends ExtendedQueryCommandBase<R>> extends QueryCommandBaseCodec<R, C> {

  private PgEncoder encoder;

  private static final String TABLE_SCHEMA_CHANGE_ERROR_MESSAGE_PATTERN = "bind message has \\d result formats but query has \\d columns";

  ExtendedQueryCommandBaseCodec(C cmd) {
    super(cmd);
    decoder = new RowResultDecoder<>(cmd.collector(), ((PgPreparedStatement)cmd.preparedStatement()).rowDesc());
  }

  @Override
  void encode(PgEncoder encoder) {
    this.encoder = encoder;
  }

  @Override
  void handleRowDescription(PgRowDesc rowDescription) {
    decoder = new RowResultDecoder<>(cmd.collector(), rowDescription);
  }

  @Override
  void handleParseComplete() {
    // Response to Parse
  }

  @Override
  void handlePortalSuspended() {
    Throwable failure = decoder.complete();
    R result = decoder.result();
    RowDesc desc = decoder.desc;
    int size = decoder.size();
    decoder.reset();
    this.result = true;
    cmd.resultHandler().handleResult(0, size, desc, result, failure);
  }

  @Override
  void handleBindComplete() {
    // Response to Bind
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    if (cmd.preparedStatement().cacheable() && errorResponse.getMessage().matches(TABLE_SCHEMA_CHANGE_ERROR_MESSAGE_PATTERN)) {
      encoder.channelHandlerContext().fireChannelRead(new InvalidCachedStatementExecutionEvent(cmd.preparedStatement()));
    }
    super.handleErrorResponse(errorResponse);


  }
}

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

import io.vertx.sqlclient.internal.TupleInternal;
import io.vertx.sqlclient.impl.codec.InvalidCachedStatementEvent;
import io.vertx.sqlclient.internal.RowDesc;
import io.vertx.sqlclient.internal.command.CommandResponse;
import io.vertx.sqlclient.internal.command.ExtendedQueryCommand;

class ExtendedQueryCommandCodec<R, C extends ExtendedQueryCommand<R>> extends QueryCommandBaseCodec<R, C> {

  private PgEncoder encoder;

  private static final String TABLE_SCHEMA_CHANGE_ERROR_MESSAGE_PATTERN = "bind message has \\d result formats but query has \\d columns";

  ExtendedQueryCommandCodec(C cmd) {
    super(cmd);
    rowDecoder = new RowResultDecoder<>(cmd.collector(), ((PgPreparedStatement)cmd.preparedStatement()).rowDesc());
  }

  @Override
  void encode(PgEncoder encoder) {
    this.encoder = encoder;
    if (cmd.isSuspended()) {
      encoder.writeExecute(cmd.cursorId(), cmd.fetch());
      encoder.writeSync();
    } else {
      PgPreparedStatement ps = (PgPreparedStatement) cmd.preparedStatement();
      if (cmd.isBatch()) {
        if (cmd.paramsList().isEmpty()) {
          // We set suspended to false as we won't get a command complete command back from Postgres
          this.result = false;
          this.decoder.fireCommandResponse(CommandResponse.failure("Can not execute batch query with 0 sets of batch parameters."));
          return;
        } else {
          if (encoder.useLayer7Proxy) {
            encoder.writeParse(ps.sql, ps.bind.statement, new DataType[0]);
          }
          for (TupleInternal param : cmd.paramsList()) {
            encoder.writeBind(ps.bind, cmd.cursorId(), param);
            encoder.writeExecute(cmd.cursorId(), cmd.fetch());
          }
        }
      } else {
        if (encoder.useLayer7Proxy && ps.bind.statement.length == 1) {
          encoder.writeParse(ps.sql, ps.bind.statement, new DataType[0]);
        }
        encoder.writeBind(ps.bind, cmd.cursorId(), cmd.params());
        encoder.writeExecute(cmd.cursorId(), cmd.fetch());
      }
      encoder.writeSync();
    }
  }

  @Override
  void handleParseComplete() {
    // Response to Parse
  }

  @Override
  void handlePortalSuspended() {
    Throwable failure = rowDecoder.complete();
    R result = rowDecoder.result();
    RowDesc desc = rowDecoder.desc;
    int size = rowDecoder.size();
    rowDecoder.reset();
    this.result = true;
    cmd.resultHandler().handleResult(0, size, desc, result, failure);
  }

  @Override
  void handleBindComplete() {
    // Response to Bind
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    if (((PgPreparedStatement)cmd.preparedStatement()).isCached() && isTableSchemaErrorMessage(errorResponse)) {
      encoder.channelHandlerContext().fireChannelRead(new InvalidCachedStatementEvent(cmd.preparedStatement().sql()));
    }
    super.handleErrorResponse(errorResponse);
  }

  private boolean isTableSchemaErrorMessage(ErrorResponse errorResponse) {
    return errorResponse.getMessage().matches(TABLE_SCHEMA_CHANGE_ERROR_MESSAGE_PATTERN) || errorResponse.getMessage().equals("cached plan must not change result type");
  }
}

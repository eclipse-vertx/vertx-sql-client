/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.mysqlclient.MySQLBatchException;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.TupleInternal;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.util.List;

import static io.vertx.mysqlclient.impl.protocol.Packets.EnumCursorType.CURSOR_TYPE_NO_CURSOR;

class ExtendedBatchQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {

  private final List<TupleInternal> params;
  private int batchIdx = 0;

  ExtendedBatchQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd);
    params = cmd.paramsList();
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);

    if (params.isEmpty() && statement.paramDesc.paramDefinitions().length > 0) {
      encoder.handleCommandResponse(CommandResponse.failure("Statement parameter is not set because of the empty batch param list"));
      return;
    }
    encoder.socketConnection.suspendPipeline();
    doExecuteBatch();
    // Close managed prepare statement
    MySQLPreparedStatement ps = (MySQLPreparedStatement) this.cmd.ps;
    if (ps.closeAfterUsage) {
      sendCloseStatementCommand(ps);
    }
  }

  @Override
  void handleErrorPacketPayload(ByteBuf payload) {
    MySQLException mySQLException = decodeErrorPacketPayload(payload);
    reportError(batchIdx, mySQLException);
    // state needs to be reset
    commandHandlerState = CommandHandlerState.INIT;
    batchIdx++;
  }

  @Override
  protected void handleSingleResultsetDecodingCompleted(int serverStatusFlags, int affectedRows, long lastInsertId) {
    batchIdx++;
    super.handleSingleResultsetDecodingCompleted(serverStatusFlags, affectedRows, lastInsertId);
  }

  @Override
  protected boolean isDecodingCompleted(int serverStatusFlags) {
    return super.isDecodingCompleted(serverStatusFlags) && batchIdx == params.size();
  }

  @Override
  protected void handleAllResultsetDecodingCompleted() {
    encoder.socketConnection.resumePipeline();
    super.handleAllResultsetDecodingCompleted();
  }

  private void doExecuteBatch() {
    for (int i = 0; i < params.size(); i++) {
      Tuple param = params.get(i);
      sequenceId = 0;
      // binding parameters
      String bindMsg = statement.bindParameters(param);
      if (bindMsg != null) {
        reportError(i, new NoStackTraceThrowable(bindMsg));
      } else {
        sendStatementExecuteCommand(statement, statement.sendTypesToServer(), param, CURSOR_TYPE_NO_CURSOR);
      }
    }
  }

  private void reportError(int iteration, Throwable error) {
    if (failure == null) {
      failure = new MySQLBatchException();
    }
    ((MySQLBatchException) failure).reportError(iteration, error);
  }
}

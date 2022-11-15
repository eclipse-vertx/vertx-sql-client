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

import static io.vertx.mysqlclient.impl.protocol.Packets.EnumCursorType.*;

class ExtendedBatchQueryCommandCodec<R> extends ExtendedQueryCommandBaseCodec<R, ExtendedQueryCommand<R>> {

  private final List<TupleInternal> params;
  private final boolean[] bindingFailures;
  private boolean pipeliningEnabled;
  private int sent;
  private int received;

  ExtendedBatchQueryCommandCodec(ExtendedQueryCommand<R> cmd) {
    super(cmd);
    params = cmd.paramsList();
    bindingFailures = new boolean[params.size()];
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    if (params.isEmpty() && statement.paramDesc.paramDefinitions().length > 0) {
      encoder.handleCommandResponse(CommandResponse.failure("Statement parameter is not set because of the empty batch param list"));
      return;
    }
    pipeliningEnabled = encoder.socketConnection.pipeliningEnabled();
    encoder.socketConnection.suspendPipeline();
    doExecuteBatch();
  }

  @Override
  void handleErrorPacketPayload(ByteBuf payload) {
    skipBindingFailures();
    MySQLException mySQLException = decodeErrorPacketPayload(payload);
    reportError(received++, mySQLException);
    // state needs to be reset
    commandHandlerState = CommandHandlerState.INIT;
    if (received == params.size()) {
      super.closePreparedStatement();
      encoder.socketConnection.resumePipeline();
      encoder.handleCommandResponse(CommandResponse.failure(failure));
    } else {
      doExecuteBatch();
    }
  }

  private void skipBindingFailures() {
    while (bindingFailures[received]) {
      received++;
    }
  }

  @Override
  protected void handleSingleResultsetDecodingCompleted(int serverStatusFlags, int affectedRows, long lastInsertId) {
    skipBindingFailures();
    received++;
    super.handleSingleResultsetDecodingCompleted(serverStatusFlags, affectedRows, lastInsertId);
    doExecuteBatch();
  }

  @Override
  protected boolean isDecodingCompleted(int serverStatusFlags) {
    return super.isDecodingCompleted(serverStatusFlags) && received == params.size();
  }

  @Override
  protected void handleAllResultsetDecodingCompleted() {
    encoder.socketConnection.resumePipeline();
    super.closePreparedStatement();
    super.handleAllResultsetDecodingCompleted();
  }

  @Override
  protected void closePreparedStatement() {
    // Handled manually at the end of all executions
  }

  private void doExecuteBatch() {
    while (sent < params.size()) {
      Tuple param = params.get(sent);
      sequenceId = 0;
      // binding parameters
      String bindMsg = statement.bindParameters(param);
      if (bindMsg != null) {
        bindingFailures[sent] = true;
        reportError(sent, new NoStackTraceThrowable(bindMsg));
        sent++;
      } else {
        sendStatementExecuteCommand(statement, statement.sendTypesToServer(), param, CURSOR_TYPE_NO_CURSOR);
        sent++;
        if (!pipeliningEnabled) {
          break;
        }
      }
    }
  }

  private void reportError(int iteration, Throwable error) {
    MySQLBatchException batchException = (MySQLBatchException) failure;
    if (batchException == null) {
      failure = batchException = new MySQLBatchException();
    }
    batchException.reportError(iteration, error);
  }
}

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

import io.netty.buffer.ByteBuf;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.codec.CommandMessage;
import io.vertx.sqlclient.codec.CommandResponse;
import io.vertx.sqlclient.spi.protocol.CloseConnectionCommand;
import io.vertx.sqlclient.spi.protocol.CloseCursorCommand;
import io.vertx.sqlclient.spi.protocol.CloseStatementCommand;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.InitCommand;
import io.vertx.sqlclient.spi.protocol.PrepareStatementCommand;
import io.vertx.sqlclient.spi.protocol.SimpleQueryCommand;

import java.util.Arrays;

public abstract class PgCommandMessage<R, C extends CommandBase<R>> extends CommandMessage<R, C> {

  private static final Logger logger = LoggerFactory.getLogger(PgCommandMessage.class);

  PgDecoder decoder;
  PgException failure;
  R result;

  PgCommandMessage(C cmd) {
    super(cmd);
  }

  public static PgCommandMessage<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof InitCommand) {
      return new InitPgCommandMessage((InitCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand<?>) {
      return new SimpleQueryPgCommandMessage<>((SimpleQueryCommand<?>) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      return new PrepareStatementPgCommandMessage((PrepareStatementCommand) cmd);
    } else if (cmd instanceof CloseConnectionCommand) {
      return CloseConnectionPgCommandMessage.INSTANCE;
    } else if (cmd instanceof CloseCursorCommand) {
      return new ClosePortalPgCommandMessage((CloseCursorCommand) cmd);
    } else if (cmd instanceof CloseStatementCommand) {
      return new CloseStatementPgCommandMessage((CloseStatementCommand) cmd);
    }
    throw new AssertionError("Invalid command " + cmd);
  }

  abstract void encode(PgEncoder encoder);

  void handleBackendKeyData(int processId, int secretKey) {
    logger.warn(getClass().getSimpleName() + " should handle message BackendKeyData");
  }

  void handleEmptyQueryResponse() {
    logger.warn(getClass().getSimpleName() + " should handle message EmptyQueryResponse");
  }

  void handleParameterDescription(PgParamDesc paramDesc) {
    logger.warn(getClass().getSimpleName() + " should handle message ParameterDescription");
  }

  void handleParseComplete() {
    logger.warn(getClass().getSimpleName() + " should handle message ParseComplete");
  }

  void handleCloseComplete() {
    logger.warn(getClass().getSimpleName() + " should handle message CloseComplete");
  }

  void handleRowDescription(PgColumnDesc[] columnDescs) {
    logger.warn(getClass().getSimpleName() + " should handle message " + Arrays.asList(columnDescs));
  }

  void handleNoData() {
    logger.warn(getClass().getSimpleName() + " should handle message NoData");
  }

  void handleErrorResponse(ErrorResponse errorResponse) {
    logger.warn(getClass().getSimpleName() + " should handle message " + errorResponse);
  }

  void handlePortalSuspended() {
    logger.warn(getClass().getSimpleName() + " should handle message PortalSuspended");
  }

  void handleBindComplete() {
    logger.warn(getClass().getSimpleName() + " should handle message BindComplete");
  }

  void handleCommandComplete(int updated) {
    logger.warn(getClass().getSimpleName() + " should handle message CommandComplete");
  }

  void handleAuthenticationMD5Password(byte[] salt) {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationMD5Password");
  }

  void handleAuthenticationSasl(ByteBuf in) {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationSasl");
  }

  void handleAuthenticationSaslContinue(ByteBuf in) {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationSaslContinue");
  }

  void handleAuthenticationSaslFinal(ByteBuf in) {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationSaslFinal");
  }

  void handleAuthenticationClearTextPassword() {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationClearTextPassword");
  }

  void handleAuthenticationOk() {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationOk");
  }

  void handleParameterStatus(String key, String value) {
    logger.debug("Parameter " + key + " changed to " + value);
  }

  /**
   * <p>
   * The frontend can issue commands. Every message returned from the backend has transaction status
   * that would be one of the following
   * <p>
   * IDLE : Not in a transaction block
   * <p>
   * ACTIVE : In transaction block
   * <p>
   * FAILED : Failed transaction block (queries will be rejected until block is ended)
   */
  void handleReadyForQuery() {
    CommandResponse<R> resp;
    if (failure != null) {
      resp = CommandResponse.failure(failure);
    } else {
      resp = CommandResponse.success(result);
    }
    decoder.fireCommandResponse(resp);
  }
}

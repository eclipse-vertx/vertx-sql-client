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
import io.vertx.sqlclient.impl.CommandMessage;
import io.vertx.sqlclient.internal.command.CloseConnectionCommand;
import io.vertx.sqlclient.internal.command.CloseCursorCommand;
import io.vertx.sqlclient.internal.command.CloseStatementCommand;
import io.vertx.sqlclient.internal.command.CommandBase;
import io.vertx.sqlclient.impl.CommandResponse;
import io.vertx.sqlclient.internal.command.ExtendedQueryCommand;
import io.vertx.sqlclient.internal.command.InitCommand;
import io.vertx.sqlclient.internal.command.PrepareStatementCommand;
import io.vertx.sqlclient.internal.command.SimpleQueryCommand;

import java.util.Arrays;

public abstract class PgCommandCodec<R, C extends CommandBase<R>> extends CommandMessage<R, C> {

  private static final Logger logger = LoggerFactory.getLogger(PgCommandCodec.class);

  PgDecoder decoder;
  PgException failure;
  R result;

  PgCommandCodec(C cmd) {
    super(cmd);
  }

  public static PgCommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof InitCommand) {
      return new InitCommandCodec((InitCommand) cmd);
    } else if (cmd instanceof SimpleQueryCommand<?>) {
      return new SimpleQueryCodec<>((SimpleQueryCommand<?>) cmd);
    } else if (cmd instanceof ExtendedQueryCommand<?>) {
      return new ExtendedQueryCommandCodec<>((ExtendedQueryCommand<?>) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      return new PrepareStatementCommandCodec((PrepareStatementCommand) cmd);
    } else if (cmd instanceof CloseConnectionCommand) {
      return CloseConnectionCommandCodec.INSTANCE;
    } else if (cmd instanceof CloseCursorCommand) {
      return new ClosePortalCommandCodec((CloseCursorCommand) cmd);
    } else if (cmd instanceof CloseStatementCommand) {
      return new CloseStatementCommandCodec((CloseStatementCommand) cmd);
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

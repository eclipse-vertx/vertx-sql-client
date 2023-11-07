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

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

abstract class PgCommandCodec<R, C extends CommandBase<R>> {

  private static final Logger logger = LoggerFactory.getLogger(PgCommandCodec.class);

  PgDecoder decoder;
  PgException failure;
  R result;
  final C cmd;

  PgCommandCodec(C cmd) {
    this.cmd = cmd;
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

  void handleNoticeResponse(NoticeResponse noticeResponse) {
    decoder.fireNoticeResponse(noticeResponse);
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

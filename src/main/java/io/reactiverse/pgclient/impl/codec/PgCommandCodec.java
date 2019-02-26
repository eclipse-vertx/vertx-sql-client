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
package io.reactiverse.pgclient.impl.codec;

import io.reactiverse.pgclient.impl.TxStatus;
import io.reactiverse.pgclient.impl.command.CommandResponse;
import io.reactiverse.pgclient.impl.command.CommandBase;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

abstract class PgCommandCodec<R, C extends CommandBase<R>> {

  private static final Logger logger = LoggerFactory.getLogger(PgCommandCodec.class);

  Handler<? super CommandResponse<R>> completionHandler;
  Handler<NoticeResponse> noticeHandler;
  Throwable failure;
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

  void handleRowDescription(PgRowDesc rowDescription) {
    logger.warn(getClass().getSimpleName() + " should handle message " + rowDescription);
  }

  void handleNoData() {
    logger.warn(getClass().getSimpleName() + " should handle message NoData");
  }

  void handleNoticeResponse(NoticeResponse noticeResponse) {
    noticeHandler.handle(noticeResponse);
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

  void handleAuthenticationClearTextPassword() {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationClearTextPassword");
  }

  void handleAuthenticationOk() {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationOk");
  }

  void handleParameterStatus(String key, String value) {
    logger.warn(getClass().getSimpleName() + " should handle message ParameterStatus");
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
  void handleReadyForQuery(TxStatus txStatus) {
    CommandResponse<R> resp;
    if (failure != null) {
      resp = CommandResponse.failure(failure, txStatus);
    } else {
      resp = CommandResponse.success(result, txStatus);
    }
    completionHandler.handle(resp);
  }
}

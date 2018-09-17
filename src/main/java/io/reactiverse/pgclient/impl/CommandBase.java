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

import io.reactiverse.pgclient.impl.codec.TxStatus;
import io.reactiverse.pgclient.impl.codec.decoder.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.decoder.NoticeResponse;
import io.reactiverse.pgclient.impl.codec.decoder.ParameterDescription;
import io.reactiverse.pgclient.impl.codec.decoder.RowDescription;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public abstract class CommandBase<R> {

  private static final Logger logger = LoggerFactory.getLogger(CommandBase.class);

  public Handler<? super CommandResponse<R>> completionHandler;
  public Handler<NoticeResponse> noticeHandler;
  Handler<? super CommandResponse<R>> handler;
  Throwable failure;
  R result;

  public CommandBase(Handler<? super CommandResponse<R>> handler) {
    this.handler = handler;
  }

  public void handleBackendKeyData(int processId, int secretKey) {
    logger.warn(getClass().getSimpleName() + " should handle message BackendKeyData");
  }

  public void handleEmptyQueryResponse() {
    logger.warn(getClass().getSimpleName() + " should handle message EmptyQueryResponse");
  }

  public void handleParameterDescription(ParameterDescription parameterDesc) {
    logger.warn(getClass().getSimpleName() + " should handle message " + parameterDesc);
  }

  public void handleParseComplete() {
    logger.warn(getClass().getSimpleName() + " should handle message ParseComplete");
  }

  public void handleCloseComplete() {
    logger.warn(getClass().getSimpleName() + " should handle message CloseComplete");
  }

  public void handleRowDescription(RowDescription rowDescription) {
    logger.warn(getClass().getSimpleName() + " should handle message " + rowDescription);
  }

  public void handleNoData() {
    logger.warn(getClass().getSimpleName() + " should handle message NoData");
  }

  public void handleNoticeResponse(NoticeResponse noticeResponse) {
    noticeHandler.handle(noticeResponse);
  }

  public void handleErrorResponse(ErrorResponse errorResponse) {
    logger.warn(getClass().getSimpleName() + " should handle message " + errorResponse);
  }

  public void handlePortalSuspended() {
    logger.warn(getClass().getSimpleName() + " should handle message PortalSuspended");
  }

  public void handleBindComplete() {
    logger.warn(getClass().getSimpleName() + " should handle message BindComplete");
  }

  public void handleCommandComplete(int updated) {
    logger.warn(getClass().getSimpleName() + " should handle message CommandComplete");
  }

  public void handleAuthenticationMD5Password(byte[] salt) {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationMD5Password");
  }

  public void handleAuthenticationClearTextPassword() {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationClearTextPassword");
  }

  public void handleAuthenticationOk() {
    logger.warn(getClass().getSimpleName() + " should handle message AuthenticationOk");
  }

  public void handleParameterStatus(String key, String value) {
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
  public void handleReadyForQuery(TxStatus txStatus) {
    CommandResponse<R> resp;
    if (failure != null) {
      resp = CommandResponse.failure(this.failure, txStatus);
    } else {
      resp = CommandResponse.success(result, txStatus);
    }
    completionHandler.handle(resp);
  }

  abstract void exec(MessageEncoder out);

  final void fail(Throwable err) {
    handler.handle(CommandResponse.failure(err));
  }
}

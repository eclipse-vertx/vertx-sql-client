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

import io.reactiverse.pgclient.PgException;
import io.reactiverse.pgclient.impl.codec.TxStatus;
import io.reactiverse.pgclient.impl.codec.decoder.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.reactiverse.pgclient.impl.codec.encoder.PasswordMessage;
import io.reactiverse.pgclient.impl.codec.encoder.StartupMessage;
import io.vertx.core.Handler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Initialize the connection so it can be used to interact with the database.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class InitCommand extends CommandBase<Connection> {

  private final SocketConnection conn;
  private final String username;
  private final String password;
  private final String database;
  private String encoding;
  private MessageEncoder out;

  InitCommand(
    SocketConnection conn,
    String username,
    String password,
    String database,
    Handler<? super CommandResponse<Connection>> handler) {
    super(handler);
    this.conn = conn;
    this.username = username;
    this.password = password;
    this.database = database;
  }

  @Override
  void exec(MessageEncoder out) {
    this.out = out;
    out.writeStartupMessage(new StartupMessage(username, database));
  }

  @Override
  public void handleAuthenticationMD5Password(byte[] salt) {
    out.writePasswordMessage(new PasswordMessage(username, password, salt));
    out.flush();
  }

  @Override
  public void handleAuthenticationClearTextPassword() {
    out.writePasswordMessage(new PasswordMessage(username, password, null));
    out.flush();
  }

  @Override
  public void handleAuthenticationOk() {
//      handler.handle(Future.succeededFuture(conn));
//      handler = null;
  }

  @Override
  public void handleParameterStatus(String key, String value) {
    if(key.equals("client_encoding")) {
      encoding = value;
    }
  }

  @Override
  public void handleBackendKeyData(int processId, int secretKey) {
  }

  @Override
  public void handleErrorResponse(ErrorResponse errorResponse) {
    CommandResponse<Connection> resp = CommandResponse.failure(new PgException(errorResponse));
    completionHandler.handle(resp);
  }

  @Override
  public void handleReadyForQuery(TxStatus txStatus) {
    // The final phase before returning the connection
    // We should make sure we are supporting only UTF8
    // https://www.postgresql.org/docs/9.5/static/multibyte.html#MULTIBYTE-CHARSET-SUPPORTED
    Charset cs = null;
    try {
      cs = Charset.forName(encoding);
    } catch (Exception ignore) {
    }
    CommandResponse<Connection> fut;
    if(cs == null || !cs.equals(StandardCharsets.UTF_8)) {
      fut = CommandResponse.failure(encoding + " is not supported in the client only UTF8");
    } else {
      fut = CommandResponse.success(conn);
    }
    completionHandler.handle(fut);
  }
}

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
import io.reactiverse.pgclient.impl.codec.decoder.InboundMessage;
import io.reactiverse.pgclient.impl.codec.decoder.message.AuthenticationClearTextPassword;
import io.reactiverse.pgclient.impl.codec.decoder.message.AuthenticationMD5Password;
import io.reactiverse.pgclient.impl.codec.decoder.message.AuthenticationOk;
import io.reactiverse.pgclient.impl.codec.decoder.message.BackendKeyData;
import io.reactiverse.pgclient.impl.codec.decoder.message.ErrorResponse;
import io.reactiverse.pgclient.impl.codec.decoder.message.ParameterStatus;
import io.reactiverse.pgclient.impl.codec.decoder.message.ReadyForQuery;
import io.reactiverse.pgclient.impl.codec.encoder.MessageEncoder;
import io.reactiverse.pgclient.impl.codec.encoder.PasswordMessage;
import io.reactiverse.pgclient.impl.codec.encoder.StartupMessage;
import io.vertx.core.Handler;

/**
 * Initialize the connection so it can be used to interact with the database.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class InitCommand extends CommandBase<Connection> {

  private static final String UTF8 = "UTF8";
  final SocketConnection conn;
  final String username;
  final String password;
  final String database;
  private String CLIENT_ENCODING;
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
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == AuthenticationMD5Password.class) {
      AuthenticationMD5Password authMD5 = (AuthenticationMD5Password) msg;
      out.writePasswordMessage(new PasswordMessage(username, password, authMD5.getSalt()));
      out.flush();
    } else if (msg.getClass() == AuthenticationClearTextPassword.class) {
      out.writePasswordMessage(new PasswordMessage(username, password, null));
      out.flush();
    } else if (msg.getClass() == AuthenticationOk.class) {
//      handler.handle(Future.succeededFuture(conn));
//      handler = null;
    } else if (msg.getClass() == ParameterStatus.class) {
      ParameterStatus paramStatus = (ParameterStatus) msg;
      if(paramStatus.getKey().equals("client_encoding")) {
        CLIENT_ENCODING = paramStatus.getValue();
      }
    } else if (msg.getClass() == BackendKeyData.class) {
    }  else if (msg.getClass() == ErrorResponse.class) {
      ErrorResponse error = (ErrorResponse) msg;
      CommandResponse<Connection> resp = CommandResponse.failure(new PgException(error));
      completionHandler.handle(resp);
    } else if (msg.getClass() == ReadyForQuery.class) {
      // The final phase before returning the connection
      // We should make sure we are supporting only UTF8
      // https://www.postgresql.org/docs/9.5/static/multibyte.html#MULTIBYTE-CHARSET-SUPPORTED
      CommandResponse<Connection> fut;
      if(!CLIENT_ENCODING.equals(UTF8)) {
        fut = CommandResponse.failure(CLIENT_ENCODING + " is not supported in the client only " + UTF8);
      } else {
        fut = CommandResponse.success(conn);
      }
      completionHandler.handle(fut);
    } else {
      super.handleMessage(msg);
    }
  }
}

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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgException;
import com.julienviet.pgclient.codec.decoder.DecodeContext;
import com.julienviet.pgclient.codec.decoder.InboundMessage;
import com.julienviet.pgclient.codec.decoder.message.AuthenticationClearTextPassword;
import com.julienviet.pgclient.codec.decoder.message.AuthenticationMD5Password;
import com.julienviet.pgclient.codec.decoder.message.AuthenticationOk;
import com.julienviet.pgclient.codec.decoder.message.BackendKeyData;
import com.julienviet.pgclient.codec.decoder.message.ErrorResponse;
import com.julienviet.pgclient.codec.decoder.message.ParameterStatus;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.decoder.message.SSLResponse;
import com.julienviet.pgclient.codec.encoder.message.PasswordMessage;
import com.julienviet.pgclient.codec.encoder.message.SSLRequest;
import com.julienviet.pgclient.codec.encoder.message.StartupMessage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * Initialize the connection so it can be used to interact with the database.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class InitCommand extends CommandBase {

  private static final String UTF8 = "UTF8";
  private final Handler<AsyncResult<Connection>> handler;
  final String username;
  final String password;
  final String database;
  final boolean ssl;
  private String CLIENT_ENCODING;
  private SocketConnection conn;

  InitCommand(String username, String password, String database, boolean ssl, Handler<AsyncResult<Connection>> handler) {
    this.username = username;
    this.password = password;
    this.database = database;
    this.handler = handler;
    this.ssl = ssl;
  }

  @Override
  void exec(SocketConnection c) {
    conn = c;
    conn.decodeQueue.add(new DecodeContext(false, null, null));
    if (ssl) {
      c.writeMessage(SSLRequest.INSTANCE);
    } else {
      c.writeMessage(new StartupMessage(username, database));
    }
  }

  @Override
  public void handleMessage(InboundMessage msg) {
    if (msg.getClass() == SSLResponse.class) {
      SSLResponse sslResponse = (SSLResponse) msg;
      if (sslResponse.isOk()) {
        conn.upgradeToSSL(v -> {
          conn.writeMessage(new StartupMessage(username, password));
        });
      } else {
        // This case is not tested as our test db is configured for SSL
        completionHandler.handle(null);
        handler.handle(Future.failedFuture(new RuntimeException("Postgres does not handle SSL")));
      }
    } else if (msg.getClass() == AuthenticationMD5Password.class) {
      AuthenticationMD5Password authMD5 = (AuthenticationMD5Password) msg;
      conn.writeMessage(new PasswordMessage(username, password, authMD5.getSalt()));
    } else if (msg.getClass() == AuthenticationClearTextPassword.class) {
      conn.writeMessage(new PasswordMessage(username, password, null));
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
      completionHandler.handle(null);
      handler.handle(Future.failedFuture(new PgException(error)));
    } else if (msg.getClass() == ReadyForQuery.class) {
      // The final phase before returning the connection
      // We should make sure we are supporting only UTF8
      // https://www.postgresql.org/docs/9.5/static/multibyte.html#MULTIBYTE-CHARSET-SUPPORTED
      Future<Connection> fut;
      if(!CLIENT_ENCODING.equals(UTF8)) {
        fut = Future.failedFuture(CLIENT_ENCODING + " is not supported in the client only " + UTF8);
      } else {
        fut = Future.succeededFuture(conn);
      }
      completionHandler.handle(null);
      handler.handle(fut);
    } else {
      super.handleMessage(msg);
    }
  }

  @Override
  void fail(Throwable err) {
    handler.handle(Future.failedFuture(err));
  }
}

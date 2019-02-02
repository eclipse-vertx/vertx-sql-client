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

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.NetSocketInternal;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgSocketConnection extends SocketConnectionBase {

  private final Map<String, CachedPreparedStatement> psCache;
  private final StringLongSequence psSeq = new StringLongSequence();

  int processId;
  int secretKey;

  public PgSocketConnection(NetSocketInternal socket,
                            boolean cachePreparedStatements,
                            int pipeliningLimit,
                            Context context) {
    super(socket, pipeliningLimit, context);
    this.psCache = cachePreparedStatements ? new ConcurrentHashMap<>() : null;
  }

  void sendStartupMessage(String username, String password, String database, Handler<? super CommandResponse<Connection>> completionHandler) {
    InitCommand cmd = new InitCommand(this, username, password, database);
    cmd.handler = completionHandler;
    schedule(cmd);
  }

  void sendCancelRequestMessage(int processId, int secretKey, Handler<AsyncResult<Void>> handler) {
    Buffer buffer = Buffer.buffer(16);
    buffer.appendInt(16);
    // cancel request code
    buffer.appendInt(80877102);
    buffer.appendInt(processId);
    buffer.appendInt(secretKey);

    socket.write(buffer, ar -> {
      if (ar.succeeded()) {
        // directly close this connection
        if (status == Status.CONNECTED) {
          status = Status.CLOSING;
          socket.close();
        }
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  static class CachedPreparedStatement implements Handler<CommandResponse<PreparedStatement>> {

    private CommandResponse<PreparedStatement> resp;
    private final ArrayDeque<Handler<? super CommandResponse<PreparedStatement>>> waiters = new ArrayDeque<>();

    void get(Handler<? super CommandResponse<PreparedStatement>> handler) {
      if (resp != null) {
        handler.handle(resp);
      } else {
        waiters.add(handler);
      }
    }

    @Override
    public void handle(CommandResponse<PreparedStatement> event) {
      resp = event;
      Handler<? super CommandResponse<PreparedStatement>> waiter;
      while ((waiter = waiters.poll()) != null) {
        waiter.handle(resp);
      }
    }
  }

  public NetSocketInternal socket() {
    return socket;
  }

  public boolean isSsl() {
    return socket.isSsl();
  }

  public void schedule(CommandBase<?> cmd) {
    // Special handling for cache
    if (cmd instanceof PrepareStatementCommand) {
      PrepareStatementCommand psCmd = (PrepareStatementCommand) cmd;
      Map<String, PgSocketConnection.CachedPreparedStatement> psCache = this.psCache;
      if (psCache != null) {
        PgSocketConnection.CachedPreparedStatement cached = psCache.get(psCmd.sql);
        if (cached != null) {
          Handler<? super CommandResponse<PreparedStatement>> handler = psCmd.handler;
          cached.get(handler);
          return;
        } else {
          psCmd.statement = psSeq.next();
          psCmd.cached = cached = new PgSocketConnection.CachedPreparedStatement();
          psCache.put(psCmd.sql, cached);
          Handler<? super CommandResponse<PreparedStatement>> a = psCmd.handler;
          psCmd.cached.get(a);
          psCmd.handler = psCmd.cached;
        }
      }
    }
    super.schedule(cmd);
  }

  @Override
  public int getProcessId() {
    return processId;
  }

  @Override
  public int getSecretKey() {
    return secretKey;
  }

}

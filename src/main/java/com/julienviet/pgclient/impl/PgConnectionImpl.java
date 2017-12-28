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

import com.julienviet.pgclient.*;
import com.julienviet.pgclient.impl.codec.DataType;
import io.vertx.core.*;

import java.util.stream.IntStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionImpl extends PgClientBase<PgConnectionImpl> implements PgConnection, Connection.Holder {

  private final Context context;
  public final Connection conn;
  private volatile Handler<Throwable> exceptionHandler;
  private volatile Handler<Void> closeHandler;
  private Transaction tx;
  private volatile Handler<PgNotification> notificationHandler;

  public PgConnectionImpl(Context context, Connection conn) {
    this.context = context;
    this.conn = conn;
  }

  @Override
  public Connection connection() {
    return conn;
  }

  @Override
  public void handleClosed() {
    Handler<Void> handler = closeHandler;
    if (handler != null) {
      context.runOnContext(handler);
    }
  }

  @Override
  protected void schedule(CommandBase<?> cmd) {
    Context current = Vertx.currentContext();
    if (current == context) {
      if (tx != null) {
        tx.schedule(cmd);
      } else {
        conn.schedule(cmd);
      }
    } else {
      context.runOnContext(v -> {
        schedule(cmd);
      });
    }
  }

  @Override
  public void handleException(Throwable err) {
    Handler<Throwable> handler = exceptionHandler;
    if (handler != null) {
      context.runOnContext(v -> {
        handler.handle(err);
      });
    } else {
      err.printStackTrace();
    }
  }

  @Override
  public boolean isSSL() {
    return conn.isSsl();
  }

  @Override
  public PgConnection closeHandler(Handler<Void> handler) {
    closeHandler = handler;
    return this;
  }

  @Override
  public PgConnection notificationHandler(Handler<PgNotification> handler) {
    notificationHandler = handler;
    return this;
  }

  @Override
  public PgConnection exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public PgTransaction begin() {
    if (tx != null) {
      throw new IllegalStateException();
    }
    tx = new Transaction(context, conn, v -> {
      tx = null;
    });
    return tx;
  }

  public void handleNotification(int processId, String channel, String payload) {
    Handler<PgNotification> handler = notificationHandler;
    if (handler != null) {
      handler.handle(new PgNotification().setProcessId(processId).setChannel(channel).setPayload(payload));
    }
  }

  @Override
  public void close() {
    if (tx != null) {
      tx.rollback(ar -> conn.close(this));
      tx = null;
    } else {
      conn.close(this);
    }
  }

  @Override
  public PgConnection prepare(String sql, Handler<AsyncResult<PgPreparedQuery>> handler) {
    schedule(new PrepareStatementCommand(sql, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(new PgPreparedQueryImpl(conn, ar.result())));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    }));
    return this;
  }

  public <R> PgConnection getFunction(String name, Handler<AsyncResult<PgFunction<R>>> handler) {
    query("SELECT oid, proargtypes, pronargdefaults, prorettype FROM pg_proc WHERE proname='" + name + "'", ar -> {
      if (ar.succeeded()) {
        PgResult<Row> rows = ar.result();
        if (rows.size() >= 1) {
          Row row = rows.iterator().next();
          int oid = row.getInteger(0);
          int[] argTypeOids = (int[]) row.getValue(1);
          DataType<?>[] paramTypes = IntStream.of(argTypeOids).mapToObj(DataType::valueOf).toArray(DataType[]::new);
          int defaultArgsCount = row.getInteger(2);
          int returnTypeOid = row.getInteger(3);
          DataType<?> returnType = DataType.valueOf(returnTypeOid);
          handler.handle(Future.succeededFuture(new PgFunction<>(
            conn, oid, paramTypes, returnType
          )));
        } else {
          handler.handle(Future.failedFuture("Function " + name + " does not exists"));
        }
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
    return this;
  }
}

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
package io.vertx.pgclient.impl;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgNotification;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class PgConnectionImpl extends SqlConnectionImpl<PgConnectionImpl> implements PgConnection  {

  public static void connect(Vertx vertx, PgConnectOptions options, Handler<AsyncResult<PgConnection>> handler) {
    Context ctx = Vertx.currentContext();
    if (ctx != null) {
      PgConnectionFactory client = new PgConnectionFactory(ctx, false, options);
      client.connectAndInit(ar -> {
        if (ar.succeeded()) {
          Connection conn = ar.result();
          PgConnectionImpl p = new PgConnectionImpl(client, ctx, conn);
          conn.init(p);
          handler.handle(Future.succeededFuture(p));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } else {
      vertx.runOnContext(v -> {
        if (options.isUsingDomainSocket() && !vertx.isNativeTransportEnabled()) {
          handler.handle(Future.failedFuture("Native transport is not available"));
        } else {
          connect(vertx, options, handler);
        }
      });
    }
  }

  private final PgConnectionFactory factory;
  private volatile Handler<PgNotification> notificationHandler;

  public PgConnectionImpl(PgConnectionFactory factory, Context context, Connection conn) {
    super(context, conn);

    this.factory = factory;
  }

  @Override
  public PgConnection notificationHandler(Handler<PgNotification> handler) {
    notificationHandler = handler;
    return this;
  }


  public void handleNotification(int processId, String channel, String payload) {
    Handler<PgNotification> handler = notificationHandler;
    if (handler != null) {
      handler.handle(new PgNotification().setProcessId(processId).setChannel(channel).setPayload(payload));
    }
  }

  @Override
  public int processId() {
    return conn.getProcessId();
  }

  @Override
  public int secretKey() {
    return conn.getSecretKey();
  }

  @Override
  public PgConnection cancelRequest(Handler<AsyncResult<Void>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      factory.connect(ar -> {
        if (ar.succeeded()) {
          PgSocketConnection conn = ar.result();
          conn.sendCancelRequestMessage(this.processId(), this.secretKey(), handler);
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } else {
      context.runOnContext(v -> cancelRequest(handler));
    }
    return this;
  }
}

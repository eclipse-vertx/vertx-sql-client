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

import io.vertx.core.impl.ContextInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgNotification;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.Notification;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class PgConnectionImpl extends SqlConnectionImpl<PgConnectionImpl> implements PgConnection  {

  public static Future<PgConnection> connect(ContextInternal context, PgConnectOptions options) {
    if (options.isUsingDomainSocket() && !context.owner().isNativeTransportEnabled()) {
      return context.failedFuture("Native transport is not available");
    } else {
      PgConnectionFactory client = new PgConnectionFactory(context.owner(), context, options);
      return client.connect()
        .map(conn -> {
        PgConnectionImpl pgConn = new PgConnectionImpl(client, context, conn);
        conn.init(pgConn);
        return pgConn;
      });
    }
  }

  private final PgConnectionFactory factory;
  private volatile Handler<PgNotification> notificationHandler;

  PgConnectionImpl(PgConnectionFactory factory, ContextInternal context, Connection conn) {
    super(context, conn);

    this.factory = factory;
  }

  @Override
  protected void appendQueryPlaceHolder(StringBuilder builder, int index) {
    builder.append('?').append(index);
  }

  @Override
  public PgConnection notificationHandler(Handler<PgNotification> handler) {
    notificationHandler = handler;
    return this;
  }


  public void handleEvent(Object event) {
    Handler<PgNotification> handler = notificationHandler;
    if (handler != null && event instanceof Notification) {
      Notification notification = (Notification) event;
      handler.handle(new PgNotification()
        .setChannel(notification.getChannel())
        .setProcessId(notification.getProcessId())
        .setPayload(notification.getPayload()));
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
      factory.cancelRequest(this.processId(), this.secretKey(), handler);
    } else {
      context.runOnContext(v -> cancelRequest(handler));
    }
    return this;
  }
}

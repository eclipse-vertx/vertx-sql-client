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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.internal.ContextInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgNotice;
import io.vertx.pgclient.PgNotification;
import io.vertx.pgclient.impl.codec.NoticeResponse;
import io.vertx.pgclient.impl.codec.TxFailedEvent;
import io.vertx.pgclient.spi.PgDriver;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.impl.Notification;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.internal.SqlConnectionBase;

public class PgConnectionImpl extends SqlConnectionBase<PgConnectionImpl> implements PgConnection  {

  public static Future<PgConnection> connect(ContextInternal context, PgConnectOptions options) {
    PgConnectionFactory client;
    try {
      client = new PgConnectionFactory(context.owner());
    } catch (Exception e) {
      return context.failedFuture(e);
    }
    return client.connect((Context)context, options).map(conn -> {
      PgConnectionImpl impl = new PgConnectionImpl(client, context, conn);
      conn.init(impl);
      prepareForClose(context, impl);
      return impl;
    });
  }

  private volatile Handler<PgNotification> notificationHandler;
  private volatile Handler<PgNotice> noticeHandler;

  public PgConnectionImpl(PgConnectionFactory factory, ContextInternal context, Connection conn) {
    super(context, factory, conn, PgDriver.INSTANCE);
  }

  @Override
  public PgConnection notificationHandler(Handler<PgNotification> handler) {
    notificationHandler = handler;
    return this;
  }

  public void handleEvent(Object event) {
    if (event instanceof Notification) {
      Handler<PgNotification> handler = notificationHandler;
      if (handler != null) {
        Notification notification = (Notification) event;
        handler.handle(new PgNotification()
          .setChannel(notification.getChannel())
          .setProcessId(notification.getProcessId())
          .setPayload(notification.getPayload()));
      }
    } else if (event instanceof NoticeResponse) {
      Handler<PgNotice> handler = noticeHandler;
      NoticeResponse noticeEvent = (NoticeResponse) event;
      PgNotice notice = new PgNotice()
        .setSeverity(noticeEvent.getSeverity())
        .setCode(noticeEvent.getCode())
        .setMessage(noticeEvent.getMessage())
        .setDetail(noticeEvent.getDetail())
        .setHint(noticeEvent.getHint())
        .setPosition(noticeEvent.getPosition())
        .setInternalPosition(noticeEvent.getInternalPosition())
        .setInternalQuery(noticeEvent.getInternalQuery())
        .setWhere(noticeEvent.getWhere())
        .setFile(noticeEvent.getFile())
        .setLine(noticeEvent.getLine())
        .setRoutine(noticeEvent.getRoutine())
        .setSchema(noticeEvent.getSchema())
        .setTable(noticeEvent.getTable())
        .setColumn(noticeEvent.getColumn())
        .setDataType(noticeEvent.getDataType())
        .setConstraint(noticeEvent.getConstraint());
      if (handler != null) {
        handler.handle(notice
        );
      } else {
        notice.log(SocketConnectionBase.logger);
      }
    } else if (event instanceof TxFailedEvent) {
      if (tx != null) {
        tx.fail();
      }
    }
  }

  @Override
  public PgConnection noticeHandler(Handler<PgNotice> handler) {
    noticeHandler = handler;
    return this;
  }

  @Override
  public int processId() {
    PgSocketConnection actual = (PgSocketConnection) conn.unwrap();
    return actual.getProcessId();
  }

  @Override
  public int secretKey() {
    PgSocketConnection actual = (PgSocketConnection) conn.unwrap();
    return actual.getSecretKey();
  }

  @Override
  public Future<Void> cancelRequest() {
    Promise<Void> promise = context.owner().getOrCreateContext().promise();
    context.emit(promise, p -> {
      PgSocketConnection unwrap = (PgSocketConnection) conn.unwrap();
      ((PgConnectionFactory) factory).cancelRequest(unwrap.connectOptions(), this.processId(), this.secretKey()).onComplete(p);
    });
    return promise.future();
  }
}

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

import io.vertx.core.*;
import io.vertx.core.internal.ContextInternal;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgCopyIn;
import io.vertx.pgclient.PgCopyInOptions;
import io.vertx.pgclient.PgCopyOut;
import io.vertx.pgclient.PgCopyOutOptions;
import io.vertx.pgclient.PgNotice;
import io.vertx.pgclient.PgNotification;
import io.vertx.pgclient.impl.codec.NoticeResponse;
import io.vertx.pgclient.impl.codec.TxFailedEvent;
import io.vertx.pgclient.spi.PgDriver;
import io.vertx.sqlclient.codec.SocketConnectionBase;
import io.vertx.sqlclient.internal.SqlConnectionBase;
import io.vertx.sqlclient.spi.connection.Connection;

import java.util.Locale;
import java.util.function.Supplier;

public class PgConnectionImpl extends SqlConnectionBase<PgConnectionImpl> implements PgConnection  {

  public static Future<PgConnection> connect(ContextInternal context, PgConnectOptions options) {
    PgConnectionFactory client;
    try {
      client = new PgConnectionFactory(context.owner());
    } catch (Exception e) {
      return context.failedFuture(e);
    }
    return client.connect((Context)context, options).map(conn -> {
      PgConnectionImpl impl = new PgConnectionImpl(client, context, conn, true);
      conn.init(impl);
      return impl;
    });
  }

  private volatile Handler<PgNotification> notificationHandler;
  private volatile Handler<PgNotice> noticeHandler;

  /**
   * The COPY operation currently owning the connection, {@code null} when there is none.
   * <p/>
   * Like {@link #tx} this is confined to the connection context and cleared by the operation itself
   * when the server is done with it, a connection carries at most one of them at a time.
   */
  private ActiveCopy activeCopy;

  /**
   * A COPY operation in progress, remembered so that closing the connection leaves copy mode
   * instead of recycling a connection PostgreSQL still considers to be copying.
   */
  private static final class ActiveCopy {

    private final Supplier<Future<Void>> leaveCopyMode;
    private final Future<Void> commandCompletion;

    ActiveCopy(Supplier<Future<Void>> leaveCopyMode, Future<Void> commandCompletion) {
      this.leaveCopyMode = leaveCopyMode;
      this.commandCompletion = commandCompletion;
    }

    /**
     * Tell the server to end the copy and wait for it to acknowledge, so the connection is usable
     * again. The command is expected to fail, aborting a COPY is reported as an error by the
     * server, and the connection is closing either way.
     */
    Future<Void> cancel() {
      return leaveCopyMode.get()
        .transform(ignored -> commandCompletion)
        .otherwiseEmpty();
    }
  }

  public PgConnectionImpl(PgConnectionFactory factory, ContextInternal context, Connection conn) {
    super(context, factory, conn, PgDriver.INSTANCE);
  }

  public PgConnectionImpl(PgConnectionFactory factory, ContextInternal context, Connection conn, boolean registerCleanup) {
    super(context, factory, conn, PgDriver.INSTANCE, registerCleanup);
  }

  /**
   * {@inheritDoc}
   * <p/>
   * A COPY in progress is left before the connection goes, so that a pooled connection is never
   * recycled while PostgreSQL still considers it to be copying. COPY IN is given up with
   * {@code CopyFail}, COPY OUT has no such message, the remaining rows are read and dropped, so
   * closing during a large COPY OUT waits for the server to finish sending it.
   */
  @Override
  public Future<Void> close() {
    ActiveCopy copy = activeCopy;
    if (copy == null) {
      return super.close();
    }
    return copy.cancel().compose(v -> PgConnectionImpl.super.close());
  }

  /**
   * Remember {@code copy} until the server is done with it, so that the connection is not handed
   * to another COPY, or back to a pool, while it is still in copy mode.
   */
  private void beginCopy(ActiveCopy copy) {
    activeCopy = copy;
    copy.commandCompletion.onComplete(ar -> {
      if (activeCopy == copy) {
        activeCopy = null;
      }
    });
  }

  /**
   * COPY takes over the connection, so the statement has to be the right kind of COPY, and it has
   * to be the only one: a trailing statement would run after the transfer and its
   * {@code CommandComplete} would be mistaken for the COPY's, reporting a nonsense row count.
   *
   * @return a failed future to return to the caller, or {@code null} when the statement is fine
   */
  private <T> Future<T> checkCopySql(String sql, String direction) {
    if (sql == null) {
      return context.failedFuture(new NullPointerException("sql"));
    }
    String trimmed = sql.trim();
    // Keywords are separated by any run of whitespace, a statement may well be wrapped over lines
    String normalized = trimmed.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
    if (!normalized.startsWith("COPY") || !normalized.contains(direction)) {
      return context.failedFuture(new IllegalArgumentException("Not a COPY " + direction + " statement: " + sql));
    }
    if (hasTrailingStatement(trimmed)) {
      return context.failedFuture(new IllegalArgumentException("COPY accepts a single statement: " + sql));
    }
    return null;
  }

  /**
   * Whether something follows the first statement separator, {@code COPY t FROM STDIN (DELIMITER ';')}
   * is a single statement, the semicolon belongs to the option rather than separating statements.
   */
  private static boolean hasTrailingStatement(String sql) {
    boolean inString = false;
    boolean inIdentifier = false;
    for (int i = 0; i < sql.length(); i++) {
      char c = sql.charAt(i);
      if (c == '\'' && !inIdentifier) {
        inString = !inString;
      } else if (c == '"' && !inString) {
        inIdentifier = !inIdentifier;
      } else if (c == ';' && !inString && !inIdentifier) {
        return !sql.substring(i + 1).trim().isEmpty();
      }
    }
    return false;
  }

  private <T> Future<T> copyAlreadyInProgress() {
    return context.failedFuture(new IllegalStateException("A COPY operation is already in progress on this connection"));
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
        PgNotification pgNotification = new PgNotification()
          .setChannel(notification.getChannel())
          .setProcessId(notification.getProcessId())
          .setPayload(notification.getPayload());
        context.duplicate().emit(pgNotification, handler);
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
        context.duplicate().emit(notice, handler);
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

  @Override
  public Future<PgCopyOut> copyOut(String sql) {
    return copyOut(sql, null);
  }

  @Override
  public Future<PgCopyOut> copyOut(String sql, PgCopyOutOptions options) {
    Future<PgCopyOut> rejected = checkCopySql(sql, "TO STDOUT");
    if (rejected != null) {
      return rejected;
    }

    if (activeCopy != null) {
      return copyAlreadyInProgress();
    }

    PgCopyOutOptions finalOptions = options != null ? options : new PgCopyOutOptions();
    Promise<Void> completion = context.promise();
    Future<Void> commandCompletion = completion.future();
    PgSocketConnection actual = (PgSocketConnection) conn.unwrap();
    CopyOutStreamImpl out = new CopyOutStreamImpl(context, actual.socket());
    beginCopy(new ActiveCopy(() -> {
      out.discard(new IllegalStateException("Connection closed"));
      return Future.succeededFuture();
    }, commandCompletion));

    try {
      schedule(new CopyOutStreamCommand(sql, out, finalOptions), completion);
    } catch (RuntimeException e) {
      completion.tryFail(e);
    }
    commandCompletion.onFailure(out::fail);
    // A COPY that ran without entering copy mode leaves nothing to hand out, so say so rather than
    // leaving the caller holding a future that never resolves.
    commandCompletion.onSuccess(v -> {
      if (!out.readyFuture().isComplete()) {
        out.fail(new IllegalStateException("COPY did not start: " + sql));
      }
    });

    return out.readyFuture();
  }

  @Override
  public Future<PgCopyIn> copyIn(String sql) {
    return copyIn(sql, null);
  }

  @Override
  public Future<PgCopyIn> copyIn(String sql, PgCopyInOptions options) {
    Future<PgCopyIn> rejected = checkCopySql(sql, "FROM STDIN");
    if (rejected != null) {
      return rejected;
    }

    if (activeCopy != null) {
      return copyAlreadyInProgress();
    }

    PgCopyInOptions finalOptions = options != null ? options : new PgCopyInOptions();
    Promise<Void> completion = context.promise();
    Future<Void> commandCompletion = completion.future();

    CopyInStreamInternal in = new CopyInStreamImpl(context, finalOptions);
    beginCopy(new ActiveCopy(() -> in.abort("Connection closed"), commandCompletion));

    try {
      schedule(new CopyInStreamCommand(sql, in), completion);
    } catch (RuntimeException e) {
      completion.tryFail(e);
    }
    commandCompletion.onFailure(in::failFromServer);
    // A COPY that ran without entering copy mode leaves nothing to hand out, so say so rather than
    // leaving the caller holding a future that never resolves.
    commandCompletion.onSuccess(v -> {
      if (!in.readyFuture().isComplete()) {
        in.failFromServer(new IllegalStateException("COPY did not start: " + sql));
      }
    });

    return in.readyFuture();
  }
}

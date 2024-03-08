/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.impl.commands.*;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.*;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import oracle.jdbc.OracleConnection;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.vertx.oracleclient.impl.Helper.isFatal;

public class OracleJdbcConnection implements Connection {

  private final ClientMetrics metrics;
  private final OracleConnection connection;
  private final OracleMetadata metadata;
  private final ContextInternal context;
  private final OracleConnectOptions options;
  @SuppressWarnings("rawtypes")
  private final ConcurrentMap<String, RowReader> cursors = new ConcurrentHashMap<>();
  private Holder holder;

  // Command pipeline state
  @SuppressWarnings("rawtypes")
  private final Deque<CommandBase> pending = new ArrayDeque<>();
  private Promise<Void> closePromise;
  private boolean inflight, executing;

  public OracleJdbcConnection(ContextInternal ctx, ClientMetrics metrics, OracleConnectOptions options, OracleConnection oc, OracleMetadata metadata) {
    this.context = ctx;
    this.metrics = metrics;
    this.options = options;
    this.connection = oc;
    this.metadata = metadata;
  }

  public Object createArray(String typeName, Object elements) {
	  try {
		  return connection.createARRAY( typeName, elements );
	  }
	  catch (SQLException e) {
		  throw new RuntimeException( e );
	  }
  }

  @Override
  public int pipeliningLimit() {
    return 1;
  }

  @Override
  public ClientMetrics metrics() {
    return metrics;
  }

  @Override
  public TracingPolicy tracingPolicy() {
    return options.getTracingPolicy();
  }

  @Override
  public String system() {
    return "oracle";
  }

  @Override
  public String database() {
    return options.getDatabase();
  }

  @Override
  public String user() {
    return options.getUser();
  }

  @Override
  public SocketAddress server() {
    return options.getSocketAddress();
  }

  @Override
  public void init(Holder holder) {
    this.holder = holder;
  }

  @Override
  public boolean isSsl() {
    return options.isSsl();
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public DatabaseMetadata getDatabaseMetaData() {
    return metadata;
  }

  @Override
  public void close(Holder holder, Promise<Void> promise) {
    if (Vertx.currentContext() == context) {
      Future<Void> future;
      if (closePromise == null) {
        closePromise = context.promise();
        future = closePromise.future().andThen(ar -> holder.handleClosed());
        pending.add(CloseConnectionCommand.INSTANCE);
        checkPending();
      } else {
        future = closePromise.future();
      }
      future.onComplete(promise);
    } else {
      context.runOnContext(v -> close(holder, promise));
    }
  }

  @Override
  public int getProcessId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSecretKey() {
    throw new UnsupportedOperationException();
  }

  public Future<Void> afterAcquire() {
    PromiseInternal<Void> promise = context.owner().promise();
    context.<Void>executeBlocking(prom -> {
      try {
        connection.beginRequest();
        prom.complete();
      } catch (SQLException e) {
        prom.fail(e);
      }
    }, false).onComplete(promise);
    return promise.future();
  }

  public Future<Void> beforeRecycle() {
    PromiseInternal<Void> promise = context.owner().promise();
    context.<Void>executeBlocking(prom -> {
      try {
        connection.endRequest();
        prom.complete();
      } catch (SQLException e) {
        prom.fail(e);
      }
    }, false).onComplete(promise);
    return promise.future();
  }

  @Override
  public <R> Future<R> schedule(ContextInternal context, CommandBase<R> cmd) {
    Promise<R> promise = context.promise();
    this.context.emit(v -> doSchedule(cmd, promise));
    return promise.future();
  }

  private <R> void doSchedule(CommandBase<R> cmd, Handler<AsyncResult<R>> handler) {
    cmd.handler = handler;
    if (closePromise == null) {
      pending.add(cmd);
      checkPending();
    } else {
      cmd.fail(new NoStackTraceThrowable("Connection is no longer active"));
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void checkPending() {
    if (executing) {
      return;
    }
    try {
      executing = true;
      CommandBase cmd;
      while (!inflight && (cmd = pending.poll()) != null) {
        inflight = true;
        if (metrics != null && cmd instanceof CloseConnectionCommand) {
          metrics.close();
        }
        OracleCommand action = wrap(cmd);
        Future<Void> future = action.processCommand(cmd);
        CommandBase capture = cmd;
        future.onComplete(ar -> actionComplete(capture, action, ar));
      }
    } finally {
      executing = false;
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private OracleCommand wrap(CommandBase cmd) {
    OracleCommand action;
    if (cmd instanceof SimpleQueryCommand) {
      action = OracleSimpleQueryCommand.create(connection, context, (SimpleQueryCommand) cmd);
    } else if (cmd instanceof PrepareStatementCommand) {
      action = new OraclePrepareStatementCommand(connection, context, (PrepareStatementCommand) cmd);
    } else if (cmd instanceof ExtendedQueryCommand) {
      action = forExtendedQuery((ExtendedQueryCommand) cmd);
    } else if (cmd instanceof TxCommand) {
      action = OracleTransactionCommand.create(connection, context, ((TxCommand) cmd));
    } else if (cmd instanceof CloseStatementCommand) {
      action = new OracleCloseStatementCommand(connection, context);
    } else if (cmd instanceof CloseCursorCommand) {
      CloseCursorCommand closeCursorCommand = (CloseCursorCommand) cmd;
      RowReader reader = cursors.remove(closeCursorCommand.id());
      action = new OracleCloseCursorCommand(connection, context, reader);
    } else if (cmd instanceof CloseConnectionCommand) {
      action = new OracleCloseConnectionCommand(connection, context, closePromise);
    } else {
      throw new UnsupportedOperationException(cmd.getClass().getName());
    }
    return action;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private OracleCommand forExtendedQuery(ExtendedQueryCommand cmd) {
    OracleCommand<Boolean> action;
    String cursorId = cmd.cursorId();
    if (cursorId != null) {
      RowReader rowReader = cursors.get(cursorId);
      if (rowReader != null) {
        action = OracleCursorFetchCommand.create(connection, context, cmd, rowReader);
      } else {
        action = OracleCursorQueryCommand.create(connection, context, cmd, cmd.collector(), rr -> cursors.put(cursorId, rr));
      }
    } else if (cmd.isBatch()) {
      action = new OraclePreparedBatchQuery(connection, context, cmd, cmd.collector());
    } else {
      action = new OraclePreparedQueryCommand(connection, context, cmd, cmd.collector());
    }
    return action;
  }

  private void actionComplete(CommandBase cmd, OracleCommand<?> action, AsyncResult<Void> ar) {
    inflight = false;
    Future<Void> future = Future.succeededFuture();
    if (ar.failed()) {
      Throwable cause = ar.cause();
      if (cause instanceof SQLException || (cause = cause.getCause()) instanceof SQLException) {
        SQLException sqlException = (SQLException) cause;
        if (isFatal(sqlException)) {
          Promise<Void> promise = context.promise();
          close(holder, promise);
          future = promise.future();
        }
      }
    }
    future.onComplete(ignored -> {
      action.fireResponse();
      checkPending();
    });
  }
}

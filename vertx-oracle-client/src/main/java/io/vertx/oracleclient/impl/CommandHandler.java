/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OraclePrepareOptions;
import io.vertx.oracleclient.impl.commands.PrepareStatementCommand;
import io.vertx.oracleclient.impl.commands.SimpleQueryCommand;
import io.vertx.oracleclient.impl.commands.*;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.command.*;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import oracle.jdbc.OracleConnection;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.vertx.oracleclient.impl.Helper.*;

public class CommandHandler implements Connection {
  private final OracleConnection connection;
  private final ContextInternal context;
  private final OracleConnectOptions options;
  private Holder holder;
  @SuppressWarnings("rawtypes")
  private ConcurrentMap<String, RowReader> cursors = new ConcurrentHashMap<>();

  public CommandHandler(ContextInternal ctx, OracleConnectOptions options, OracleConnection oc) {
    this.context = ctx;
    this.options = options;
    this.connection = oc;
  }

  @Override
  public SocketAddress server() {
    throw new UnsupportedOperationException("Not yet implemented");
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
    return new OracleMetadataImpl(Helper.getOrHandleSQLException(connection::getMetaData));
  }

  @Override
  public void close(Holder holder, Promise<Void> promise) {
    executeBlocking(context, () -> connection.closeAsyncOracle())
      .compose(publisher -> first(publisher, context))
      .onSuccess(v -> {
        holder.handleClosed();
        promise.complete();
      })
      .onFailure(t -> {
        holder.handleClosed();
        promise.fail(t);
      });
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

  @SuppressWarnings("unchecked")
  @Override
  public <R> Future<R> schedule(ContextInternal contextInternal, CommandBase<R> commandBase) {
    Future<R> result;
    if (commandBase instanceof io.vertx.sqlclient.impl.command.SimpleQueryCommand) {
      result = (Future<R>) handle((io.vertx.sqlclient.impl.command.SimpleQueryCommand<?>) commandBase);
    } else if (commandBase instanceof io.vertx.sqlclient.impl.command.PrepareStatementCommand) {
      result = (Future<R>) handle((io.vertx.sqlclient.impl.command.PrepareStatementCommand) commandBase);
    } else if (commandBase instanceof ExtendedQueryCommand) {
      result = (Future<R>) handle((ExtendedQueryCommand<?>) commandBase);
    } else if (commandBase instanceof TxCommand) {
      result = handle((TxCommand<R>) commandBase);
    } else if (commandBase instanceof PingCommand) {
      result = (Future<R>) handle((PingCommand) commandBase);
    } else if (commandBase instanceof CloseStatementCommand) {
      result = context.succeededFuture();
    } else if (commandBase instanceof CloseCursorCommand) {
      result = (Future<R>) handle((CloseCursorCommand) commandBase);
    } else {
      result = context.failedFuture("Not yet implemented " + commandBase);
    }
    return result.transform(ar -> {
      Promise<R> promise = contextInternal.promise();
      if (ar.succeeded()) {
        promise.complete(ar.result());
      } else {
        Throwable cause = ar.cause();
        if (cause instanceof SQLException) {
          SQLException sqlException = (SQLException) cause;
          if (isFatal(sqlException)) {
            Promise<Void> closePromise = Promise.promise();
            close(holder, closePromise);
            closePromise.future().onComplete(v -> promise.fail(sqlException));
          } else {
            promise.fail(sqlException);
          }
        } else {
          promise.fail(cause);
        }
      }
      return promise.future();
    });
  }

  private Future<Void> handle(CloseCursorCommand cmd) {
    RowReader<?, ?> reader = cursors.remove(cmd.id());
    if (reader == null) {
      return context.succeededFuture();
    }
    return reader.close();
  }

  private Future<Integer> handle(PingCommand ping) {
    return ping.execute(connection, context);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private <R> Future<Boolean> handle(io.vertx.sqlclient.impl.command.SimpleQueryCommand cmd) {
    QueryCommand<?, R> action = new SimpleQueryCommand<>(cmd, cmd.collector());
    return action.execute(connection, context);
  }

  private Future<PreparedStatement> handle(io.vertx.sqlclient.impl.command.PrepareStatementCommand command) {
    PrepareStatementCommand action = new PrepareStatementCommand(OraclePrepareOptions.createFrom(command.options()), command.sql());
    return action.execute(connection, context);
  }

  @SuppressWarnings("unchecked")
  private <R> Future<Boolean> handle(ExtendedQueryCommand<R> cmd) {
    AbstractCommand<Boolean> action;
    String cursorId = cmd.cursorId();
    if (cursorId != null) {
      RowReader<?, R> rowReader = cursors.get(cursorId);
      if (rowReader != null) {
        action = new OracleCursorFetchCommand<>(cmd, rowReader);
      } else {
        action = new OracleCursorQueryCommand<>(cmd, cmd.collector(), rr -> cursors.put(cursorId, rr));
      }
    } else if (cmd.isBatch()) {
      action = new OraclePreparedBatch<>(cmd, cmd.collector());
    } else {
      action = new OraclePreparedQuery<>(cmd, cmd.collector());
    }
    return action.execute(connection, context);
  }

  private <R> Future<R> handle(TxCommand<R> command) {
    OracleTransactionCommand<R> action = new OracleTransactionCommand<>(command);
    return action.execute(connection, context);
  }
}

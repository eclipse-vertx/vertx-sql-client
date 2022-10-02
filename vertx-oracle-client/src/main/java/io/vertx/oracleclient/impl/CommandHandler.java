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
import io.vertx.oracleclient.impl.commands.*;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.QueryResultHandler;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import io.vertx.sqlclient.impl.command.TxCommand;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import oracle.jdbc.OracleConnection;

import java.sql.SQLException;

import static io.vertx.oracleclient.impl.Helper.*;

public class CommandHandler implements Connection {
  private final OracleConnection connection;
  private final ContextInternal context;
  private final OracleConnectOptions options;
  private Holder holder;

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
    context.executeBlocking(prom -> {
      try {
        connection.beginRequest();
        prom.complete();
      } catch (SQLException e) {
        prom.fail(e);
      }
    }, false, promise);
    return promise.future();
  }

  public Future<Void> beforeRecycle() {
    PromiseInternal<Void> promise = context.owner().promise();
    context.executeBlocking(prom -> {
      try {
        connection.endRequest();
        prom.complete();
      } catch (SQLException e) {
        prom.fail(e);
      }
    }, false, promise);
    return promise.future();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> Future<R> schedule(ContextInternal contextInternal, CommandBase<R> commandBase) {
    Future<R> result;
    if (commandBase instanceof io.vertx.sqlclient.impl.command.SimpleQueryCommand) {
      result = (Future<R>) handle((io.vertx.sqlclient.impl.command.SimpleQueryCommand) commandBase);
    } else if (commandBase instanceof io.vertx.sqlclient.impl.command.PrepareStatementCommand) {
      result = (Future<R>) handle((io.vertx.sqlclient.impl.command.PrepareStatementCommand) commandBase);
    } else if (commandBase instanceof ExtendedQueryCommand) {
      result = (Future<R>) handle((ExtendedQueryCommand<?>) commandBase);
    } else if (commandBase instanceof TxCommand) {
      result = handle((TxCommand<R>) commandBase);
    } else if (commandBase instanceof PingCommand) {
      result = (Future<R>) handle((PingCommand) commandBase);
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

  private Future<Integer> handle(PingCommand ping) {
    return ping.execute(connection, context);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <R> Future<Boolean> handle(io.vertx.sqlclient.impl.command.SimpleQueryCommand command) {
    QueryCommand<?, R> action = new SimpleQueryCommand<>(command.sql(), command.collector());
    return handle(action, command.resultHandler());
  }

  private Future<PreparedStatement> handle(io.vertx.sqlclient.impl.command.PrepareStatementCommand command) {
    PrepareStatementCommand action = new PrepareStatementCommand(OraclePrepareOptions.createFrom(command.options()), command.sql());
    return action.execute(connection, context);
  }

  private <R> Future<Boolean> handle(QueryCommand<?, R> action, QueryResultHandler<R> handler) {
    Future<OracleResponse<R>> fut = action.execute(connection, context);
    return fut
      .onSuccess(ar -> ar.handle(handler)).map(false)
      .onFailure(t -> holder.handleException(t));

  }

  private <R> Future<Boolean> handle(ExtendedQueryCommand<R> command) {
    if (command.cursorId() != null) {
      QueryCommand<?, R> cmd = new OracleCursorQueryCommand<>(command, command.params());
      return cmd.execute(connection, context)
        .map(false);
    }

    QueryCommand<?, R> action =
      command.isBatch() ?
        new OraclePreparedBatch<>(command, command.collector(), command.paramsList())
        : new OraclePreparedQuery<>(command, command.collector(), command.params());

    return handle(action, command.resultHandler());
  }

  private <R> Future<R> handle(TxCommand<R> command) {
    OracleTransactionCommand<R> action = new OracleTransactionCommand<>(command);
    return action.execute(connection, context);
  }
}

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
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
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
    Helper.first(Helper.getOrHandleSQLException(connection::closeAsyncOracle), context)
      .onComplete(x -> holder.handleClosed())
      .onComplete(promise);
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
    if (Vertx.currentContext() != context) {
      throw new IllegalArgumentException();
    }
    return context.executeBlocking(prom -> {
      try {
        connection.beginRequest();
        prom.complete();
      } catch (SQLException e) {
        prom.fail(e);
      }
    }, false);
  }

  public Future<Void> beforeRecycle() {
    if (Vertx.currentContext() != context) {
      throw new IllegalArgumentException();
    }
    return context.executeBlocking(prom -> {
      try {
        connection.endRequest();
        prom.complete();
      } catch (SQLException e) {
        prom.fail(e);
      }
    }, false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R> Future<R> schedule(ContextInternal contextInternal, CommandBase<R> commandBase) {
    if (commandBase instanceof io.vertx.sqlclient.impl.command.SimpleQueryCommand) {
      return (Future<R>) handle((io.vertx.sqlclient.impl.command.SimpleQueryCommand) commandBase);
    } else if (commandBase instanceof io.vertx.sqlclient.impl.command.PrepareStatementCommand) {
      return (Future<R>) handle((io.vertx.sqlclient.impl.command.PrepareStatementCommand) commandBase);
    } else if (commandBase instanceof ExtendedQueryCommand) {
      return (Future<R>) handle((ExtendedQueryCommand<?>) commandBase);
    } else if (commandBase instanceof TxCommand) {
      return handle((TxCommand<R>) commandBase);
    } else if (commandBase instanceof PingCommand) {
      return (Future<R>) handle((PingCommand) commandBase);
    } else {
      return context.failedFuture("Not yet implemented " + commandBase);
    }
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

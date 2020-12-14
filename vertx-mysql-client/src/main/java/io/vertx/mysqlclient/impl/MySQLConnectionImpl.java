/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.mysqlclient.MySQLAuthOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLSetOption;
import io.vertx.mysqlclient.impl.command.*;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionImpl;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

public class MySQLConnectionImpl extends SqlConnectionImpl<MySQLConnectionImpl> implements MySQLConnection {

  public static Future<MySQLConnection> connect(ContextInternal ctx, MySQLConnectOptions options) {
    if (options.isUsingDomainSocket() && !ctx.owner().isNativeTransportEnabled()) {
      return ctx.failedFuture("Native transport is not available");
    }
    MySQLConnectionFactory client;
    try {
      client = new MySQLConnectionFactory(ConnectionFactory.asEventLoopContext(ctx), options);
    } catch (Exception e) {
      return ctx.failedFuture(e);
    }
    ctx.addCloseHook(client);
    QueryTracer tracer = ctx.tracer() == null ? null : new QueryTracer(ctx.tracer(), options);
    PromiseInternal<Connection> promise = ctx.promise();
    client.connect(promise);
    return promise.future().map(conn -> {
      MySQLConnectionImpl mySQLConnection = new MySQLConnectionImpl(client, ctx, conn, tracer, null);
      conn.init(mySQLConnection);
      return mySQLConnection;
    });
  }

  private final MySQLConnectionFactory factory;

  public MySQLConnectionImpl(MySQLConnectionFactory factory, ContextInternal context, Connection conn, QueryTracer tracer, ClientMetrics metrics) {
    super(context, conn, tracer, metrics);

    this.factory = factory;
  }

  @Override
  public MySQLConnection ping(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = ping();
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<Void> ping() {
    Promise<Void> promise = promise();
    schedule(new PingCommand(), promise);
    return promise.future();
  }

  @Override
  public MySQLConnection specifySchema(String schemaName, Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = specifySchema(schemaName);
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<Void> specifySchema(String schemaName) {
    Promise<Void> promise = promise();
    schedule(new InitDbCommand(schemaName), promise);
    return promise.future();
  }

  @Override
  public MySQLConnection getInternalStatistics(Handler<AsyncResult<String>> handler) {
    Future<String> fut = getInternalStatistics();
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<String> getInternalStatistics() {
    Promise<String> promise = promise();
    schedule(new StatisticsCommand(), promise);
    return promise.future();
  }

  @Override
  public MySQLConnection setOption(MySQLSetOption option, Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = setOption(option);
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<Void> setOption(MySQLSetOption option) {
    Promise<Void> promise = promise();
    schedule(new SetOptionCommand(option), promise);
    return promise.future();
  }

  @Override
  public MySQLConnection resetConnection(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = resetConnection();
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<Void> resetConnection() {
    Promise<Void> promise = promise();
    schedule(new ResetConnectionCommand(), promise);
    return promise.future();
  }

  @Override
  public MySQLConnection debug(Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = debug();
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<Void> debug() {
    Promise<Void> promise = promise();
    schedule(new DebugCommand(), promise);
    return promise.future();
  }

  @Override
  public MySQLConnection changeUser(MySQLAuthOptions options, Handler<AsyncResult<Void>> handler) {
    Future<Void> fut = changeUser(options);
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<Void> changeUser(MySQLAuthOptions options) {
    MySQLCollation collation;
    if (options.getCollation() != null) {
      // override the collation if configured
      collation = MySQLCollation.valueOfName(options.getCollation());
    } else {
      String charset = options.getCharset();
      if (charset == null) {
        collation = MySQLCollation.DEFAULT_COLLATION;
      } else {
        collation = MySQLCollation.valueOfName(MySQLCollation.getDefaultCollationFromCharsetName(charset));
      }
    }
    Buffer serverRsaPublicKey = null;
    if (options.getServerRsaPublicKeyValue() != null) {
      serverRsaPublicKey = options.getServerRsaPublicKeyValue();
    } else {
      if (options.getServerRsaPublicKeyPath() != null) {
        serverRsaPublicKey = context.owner().fileSystem().readFileBlocking(options.getServerRsaPublicKeyPath());
      }
    }
    ChangeUserCommand cmd = new ChangeUserCommand(options.getUser(), options.getPassword(), options.getDatabase(), collation, serverRsaPublicKey, options.getProperties());
    Promise<Void> promise = promise();
    schedule(cmd, promise);
    return promise.future();
  }
}

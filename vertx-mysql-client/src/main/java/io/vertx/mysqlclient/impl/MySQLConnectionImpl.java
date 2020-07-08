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

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.mysqlclient.MySQLAuthOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLSetOption;
import io.vertx.mysqlclient.impl.command.*;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionImpl;

public class MySQLConnectionImpl extends SqlConnectionImpl<MySQLConnectionImpl> implements MySQLConnection {

  public static void connect(ContextInternal ctx, MySQLConnectOptions options, Handler<AsyncResult<MySQLConnection>> handler) {
    if (options.isUsingDomainSocket() && !ctx.owner().isNativeTransportEnabled()) {
      handler.handle(Future.failedFuture("Native transport is not available"));
      return;
    }
    if (Vertx.currentContext() == ctx) {
      MySQLConnectionFactory client;
      try {
        client = new MySQLConnectionFactory(ctx, false, options);
      } catch (Exception e) {
        handler.handle(Future.failedFuture(e));
        return;
      }
      client.connect(ar -> {
        if (ar.succeeded()) {
          Connection conn = ar.result();
          MySQLConnectionImpl p = new MySQLConnectionImpl(client, ctx, conn);
          conn.init(p);
          handler.handle(Future.succeededFuture(p));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } else {
      ctx.runOnContext(v -> {
        connect(ctx, options, handler);
      });
    }
  }

  private final MySQLConnectionFactory factory;

  public MySQLConnectionImpl(MySQLConnectionFactory factory, Context context, Connection conn) {
    super(context, conn);

    this.factory = factory;
  }

  @Override
  public void handleNotification(int processId, String channel, String payload) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MySQLConnection ping(Handler<AsyncResult<Void>> handler) {
    PingCommand cmd = new PingCommand();
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection specifySchema(String schemaName, Handler<AsyncResult<Void>> handler) {
    InitDbCommand cmd = new InitDbCommand(schemaName);
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection getInternalStatistics(Handler<AsyncResult<String>> handler) {
    StatisticsCommand cmd = new StatisticsCommand();
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection setOption(MySQLSetOption option, Handler<AsyncResult<Void>> handler) {
    SetOptionCommand cmd = new SetOptionCommand(option);
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection resetConnection(Handler<AsyncResult<Void>> handler) {
    ResetConnectionCommand cmd = new ResetConnectionCommand();
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection debug(Handler<AsyncResult<Void>> handler) {
    DebugCommand cmd = new DebugCommand();
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }

  @Override
  public MySQLConnection changeUser(MySQLAuthOptions options, Handler<AsyncResult<Void>> handler) {
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
    cmd.handler = handler;
    schedule(cmd);
    return this;
  }
}

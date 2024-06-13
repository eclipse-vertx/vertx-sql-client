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

package io.vertx.mysqlclient.impl;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.ContextInternal;
import io.vertx.mysqlclient.MySQLAuthOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLSetOption;
import io.vertx.mysqlclient.impl.command.*;
import io.vertx.mysqlclient.spi.MySQLDriver;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.spi.ConnectionFactory;

public class MySQLConnectionImpl extends SqlConnectionBase<MySQLConnectionImpl> implements MySQLConnection {

  public static Future<MySQLConnection> connect(ContextInternal ctx, MySQLConnectOptions options) {
    if (options.isUsingDomainSocket() && !ctx.owner().isNativeTransportEnabled()) {
      return ctx.failedFuture("Native transport is not available");
    }
    MySQLConnectionFactory client;
    try {
      client = new MySQLConnectionFactory(ctx.owner());
    } catch (Exception e) {
      return ctx.failedFuture(e);
    }
    return prepareForClose(ctx, client.connect((Context)ctx, options)).map(MySQLConnection::cast);
  }

  public MySQLConnectionImpl(ContextInternal context, ConnectionFactory factory, Connection conn) {
    super(context, factory, conn, MySQLDriver.INSTANCE);
  }

  @Override
  public Future<Void> ping() {
    return schedule(context, new PingCommand());
  }

  @Override
  public Future<Void> specifySchema(String schemaName) {
    return schedule(context, new InitDbCommand(schemaName));
  }

  @Override
  public Future<String> getInternalStatistics() {
    return schedule(context, new StatisticsCommand());
  }

  @Override
  public Future<Void> setOption(MySQLSetOption option) {
    return schedule(context, new SetOptionCommand(option));
  }

  @Override
  public Future<Void> resetConnection() {
    return schedule(context, new ResetConnectionCommand());
  }

  @Override
  public Future<Void> debug() {
    return schedule(context, new DebugCommand());
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
    return schedule(context, cmd);
  }
}

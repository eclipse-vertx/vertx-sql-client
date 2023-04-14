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
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.TrustOptions;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactoryBase;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;

public class MySQLConnectionFactory extends ConnectionFactoryBase {

  public MySQLConnectionFactory(VertxInternal vertx, Supplier<? extends Future<? extends SqlConnectOptions>> options, boolean oneShot) {
    super(vertx, options, oneShot);
  }

  @Override
  protected Future<Connection> doConnectInternal(SqlConnectOptions options, EventLoopContext context) {
    MySQLConnectOptions mySQLOptions = MySQLConnectOptions.wrap(options);
    SslMode sslMode = mySQLOptions.isUsingDomainSocket() ? SslMode.DISABLED : mySQLOptions.getSslMode();
    switch (sslMode) {
      case VERIFY_IDENTITY:
        String hostnameVerificationAlgorithm = options.getHostnameVerificationAlgorithm();
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          return context.failedFuture(new IllegalArgumentException("Host verification algorithm must be specified under VERIFY_IDENTITY ssl-mode."));
        }
        break;
      case VERIFY_CA:
        TrustOptions trustOptions = options.getTrustOptions();
        if (trustOptions == null) {
          return context.failedFuture(new IllegalArgumentException("Trust options must be specified under " + sslMode.name() + " ssl-mode."));
        }
        break;
    }
    int capabilitiesFlag = capabilitiesFlags(mySQLOptions);
    options.setSsl(false);
    if (sslMode == SslMode.PREFERRED) {
      return doConnect(mySQLOptions, sslMode, capabilitiesFlag, context).recover(err -> doConnect(mySQLOptions, SslMode.DISABLED, capabilitiesFlag, context));
    } else {
      return doConnect(mySQLOptions, sslMode, capabilitiesFlag, context);
    }
  }

  private int capabilitiesFlags(MySQLConnectOptions options) {
    int capabilitiesFlags = CLIENT_SUPPORTED_CAPABILITIES_FLAGS;
    if (options.getDatabase() != null && !options.getDatabase().isEmpty()) {
      capabilitiesFlags |= CLIENT_CONNECT_WITH_DB;
    }
    if (options.getProperties() != null && !options.getProperties().isEmpty()) {
      capabilitiesFlags |= CLIENT_CONNECT_ATTRS;
    }
    if (!options.isUseAffectedRows()) {
      capabilitiesFlags |= CLIENT_FOUND_ROWS;
    }
    return capabilitiesFlags;
  }

  private Future<Connection> doConnect(MySQLConnectOptions options, SslMode sslMode, int initialCapabilitiesFlags, EventLoopContext context) {
    String username = options.getUser();
    String password = options.getPassword();
    String database = options.getDatabase();
    SocketAddress server = options.getSocketAddress();
    boolean cachePreparedStatements = options.getCachePreparedStatements();
    int preparedStatementCacheMaxSize = options.getPreparedStatementCacheMaxSize();
    Predicate<String> preparedStatementCacheSqlFilter = options.getPreparedStatementCacheSqlFilter();
    Map<String, String> properties = options.getProperties();

    MySQLCollation collation;
    Charset charsetEncoding;
    if (options.getCollation() != null) {
      // override the collation if configured
      collation = MySQLCollation.valueOfName(options.getCollation());
      charsetEncoding = Charset.forName(collation.mappedJavaCharsetName());
    } else {
      String charset = options.getCharset();
      if (charset == null) {
        collation = MySQLCollation.DEFAULT_COLLATION;
      } else {
        collation = MySQLCollation.valueOfName(MySQLCollation.getDefaultCollationFromCharsetName(charset));
      }
      String characterEncoding = options.getCharacterEncoding();
      if (characterEncoding == null) {
        charsetEncoding = Charset.defaultCharset();
      } else {
        charsetEncoding = Charset.forName(options.getCharacterEncoding());
      }
    }
    Buffer serverRsaPublicKey;
    if (options.getServerRsaPublicKeyValue() != null) {
      serverRsaPublicKey = options.getServerRsaPublicKeyValue();
    } else if (options.getServerRsaPublicKeyPath() != null) {
      serverRsaPublicKey = vertx.fileSystem().readFileBlocking(options.getServerRsaPublicKeyPath());
    } else {
      serverRsaPublicKey = null;
    }
    int pipeliningLimit = options.getPipeliningLimit();
    MySQLAuthenticationPlugin authenticationPlugin = options.getAuthenticationPlugin();
    Future<NetSocket> fut = netClient(options).connect(server);
    return fut.flatMap(so -> {
      VertxMetrics vertxMetrics = vertx.metricsSPI();
      ClientMetrics metrics = vertxMetrics != null ? vertxMetrics.createClientMetrics(options.getSocketAddress(), "sql", options.getMetricsName()) : null;
      MySQLSocketConnection conn = new MySQLSocketConnection((NetSocketInternal) so, metrics, options, cachePreparedStatements, preparedStatementCacheMaxSize, preparedStatementCacheSqlFilter, pipeliningLimit, context);
      conn.init();
      return Future.future(promise -> conn.sendStartupMessage(username, password, database, collation, serverRsaPublicKey, properties, sslMode, initialCapabilitiesFlags, charsetEncoding, authenticationPlugin, promise));
    });
  }

  @Override
  public Future<SqlConnection> connect(Context context, SqlConnectOptions options) {
    ContextInternal contextInternal = (ContextInternal) context;
    Promise<SqlConnection> promise = contextInternal.promise();
    connect(asEventLoopContext(contextInternal), options)
      .map(conn -> {
        MySQLConnectionImpl mySQLConnection = new MySQLConnectionImpl(contextInternal, this, conn, oneShot);
        conn.init(mySQLConnection);
        return (SqlConnection)mySQLConnection;
      })
      .onComplete(promise);
    return promise.future();
  }
}

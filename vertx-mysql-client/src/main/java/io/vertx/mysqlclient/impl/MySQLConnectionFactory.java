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
import io.vertx.core.net.*;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;

public class MySQLConnectionFactory implements ConnectionFactory {
  private final NetClient netClient;
  private final ContextInternal context;
  private final SocketAddress socketAddress;
  private final String username;
  private final String password;
  private final String database;
  private final Map<String, String> connectionAttributes;
  private final MySQLCollation collation;
  private final Charset charsetEncoding;
  private final boolean useAffectedRows;
  private final SslMode sslMode;
  private final Buffer serverRsaPublicKey;
  private final boolean cachePreparedStatements;
  private final int preparedStatementCacheSize;
  private final Predicate<String> preparedStatementCacheSqlFilter;
  private final int initialCapabilitiesFlags;

  public MySQLConnectionFactory(ContextInternal context, MySQLConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.context = context;
    this.socketAddress = options.getSocketAddress();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.connectionAttributes = options.getProperties() == null ? null : Collections.unmodifiableMap(options.getProperties());
    MySQLCollation collation;
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
    this.collation = collation;
    this.useAffectedRows = options.isUseAffectedRows();
    this.sslMode = options.isUsingDomainSocket() ? SslMode.DISABLED : options.getSslMode();

    // server RSA public key
    Buffer serverRsaPublicKey = null;
    if (options.getServerRsaPublicKeyValue() != null) {
      serverRsaPublicKey = options.getServerRsaPublicKeyValue();
    } else {
      if (options.getServerRsaPublicKeyPath() != null) {
        serverRsaPublicKey = context.owner().fileSystem().readFileBlocking(options.getServerRsaPublicKeyPath());
      }
    }
    this.serverRsaPublicKey = serverRsaPublicKey;
    this.initialCapabilitiesFlags = initCapabilitiesFlags();

    // check the SSLMode here
    switch (sslMode) {
      case VERIFY_IDENTITY:
        String hostnameVerificationAlgorithm = netClientOptions.getHostnameVerificationAlgorithm();
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          throw new IllegalArgumentException("Host verification algorithm must be specified under VERIFY_IDENTITY ssl-mode.");
        }
      case VERIFY_CA:
        TrustOptions trustOptions = netClientOptions.getTrustOptions();
        if (trustOptions == null) {
          throw new IllegalArgumentException("Trust options must be specified under " + sslMode.name() + " ssl-mode.");
        }
        break;
    }

    this.cachePreparedStatements = options.getCachePreparedStatements();
    this.preparedStatementCacheSize = options.getPreparedStatementCacheMaxSize();
    this.preparedStatementCacheSqlFilter = options.getPreparedStatementCacheSqlFilter();

    this.netClient = context.owner().createNetClient(netClientOptions);
  }

  // Called by hook
  private void close(Handler<AsyncResult<Void>> completionHandler) {
    netClient.close();
    completionHandler.handle(Future.succeededFuture());
  }

  public Future<Void> close() {
    return netClient.close();
  }

  @Override
  public Future<Connection> connect() {
    Promise<Connection> promise = context.promise();
    context.dispatch(null, v -> doConnect(promise));
    return promise.future();
  }

  private void doConnect(Promise<Connection> promise) {
    Future<NetSocket> fut = netClient.connect(socketAddress);
    fut.onComplete(ar -> {
      if (ar.succeeded()) {
        NetSocket so = ar.result();
        MySQLSocketConnection conn = new MySQLSocketConnection((NetSocketInternal) so, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, context);
        conn.init();
        conn.sendStartupMessage(username, password, database, collation, serverRsaPublicKey, connectionAttributes, sslMode, initialCapabilitiesFlags, charsetEncoding, promise);
      } else {
        promise.fail(ar.cause());
      }
    });
  }

  private int initCapabilitiesFlags() {
    int capabilitiesFlags = CLIENT_SUPPORTED_CAPABILITIES_FLAGS;
    if (database != null && !database.isEmpty()) {
      capabilitiesFlags |= CLIENT_CONNECT_WITH_DB;
    }
    if (connectionAttributes != null && !connectionAttributes.isEmpty()) {
      capabilitiesFlags |= CLIENT_CONNECT_ATTRS;
    }
    if (!useAffectedRows) {
      capabilitiesFlags |= CLIENT_FOUND_ROWS;
    }

    return capabilitiesFlags;
  }
}

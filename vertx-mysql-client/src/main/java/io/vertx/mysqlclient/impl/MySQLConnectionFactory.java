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
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.*;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.impl.Connection;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;

public class MySQLConnectionFactory {
  private final NetClient netClient;
  private final ContextInternal context;
  private final boolean registerCloseHook;
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
  private final Closeable hook;
  private MySQLAuthenticationPlugin authenticationPlugin;

  public MySQLConnectionFactory(ContextInternal context, boolean registerCloseHook, MySQLConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.context = context;
    this.registerCloseHook = registerCloseHook;
    this.hook = this::close;
    if (registerCloseHook) {
      context.addCloseHook(hook);
    }

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
    this.authenticationPlugin = options.getAuthenticationPlugin();

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

  void close() {
    if (registerCloseHook) {
      context.removeCloseHook(hook);
    }
    netClient.close();
  }

  public void connect(Handler<AsyncResult<Connection>> handler) {
    Promise<NetSocket> promise = Promise.promise();
    promise.future().setHandler(ar1 -> {
      if (ar1.succeeded()) {
        NetSocketInternal socket = (NetSocketInternal) ar1.result();
        MySQLSocketConnection conn = new MySQLSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, context);
        conn.init();
        conn.sendStartupMessage(username, password, database, collation, serverRsaPublicKey, connectionAttributes, sslMode, initialCapabilitiesFlags, charsetEncoding, authenticationPlugin, handler);
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    netClient.connect(socketAddress, promise);
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

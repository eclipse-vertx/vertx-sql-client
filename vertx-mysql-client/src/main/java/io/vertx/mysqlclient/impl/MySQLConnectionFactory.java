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

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.*;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;

import java.nio.charset.Charset;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;

public class MySQLConnectionFactory extends SqlConnectionFactoryBase implements ConnectionFactory {

  private MySQLCollation collation;
  private Charset charsetEncoding;
  private boolean useAffectedRows;
  private SslMode sslMode;
  private Buffer serverRsaPublicKey;
  private int initialCapabilitiesFlags;
  private MySQLAuthenticationPlugin authenticationPlugin;

  public MySQLConnectionFactory(ContextInternal context, MySQLConnectOptions options) {
    super(context, options);
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions connectOptions) {
    if (!(connectOptions instanceof MySQLConnectOptions)) {
      throw new IllegalArgumentException("mismatched connect options type");
    }
    MySQLConnectOptions options = (MySQLConnectOptions) connectOptions;
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
        String hostnameVerificationAlgorithm = options.getHostnameVerificationAlgorithm();
        if (hostnameVerificationAlgorithm == null || hostnameVerificationAlgorithm.isEmpty()) {
          throw new IllegalArgumentException("Host verification algorithm must be specified under VERIFY_IDENTITY ssl-mode.");
        }
      case VERIFY_CA:
        TrustOptions trustOptions = options.getTrustOptions();
        if (trustOptions == null) {
          throw new IllegalArgumentException("Trust options must be specified under " + sslMode.name() + " ssl-mode.");
        }
        break;
    }
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    netClientOptions.setSsl(false);
  }

  @Override
  protected void doConnectInternal(Promise<Connection> promise) {
    Future<NetSocket> fut = netClient.connect(socketAddress);
    fut.onComplete(ar -> {
      if (ar.succeeded()) {
        NetSocket so = ar.result();
        MySQLSocketConnection conn = new MySQLSocketConnection((NetSocketInternal) so, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlFilter, context);
        conn.init();
        conn.sendStartupMessage(username, password, database, collation, serverRsaPublicKey, properties, sslMode, initialCapabilitiesFlags, charsetEncoding, authenticationPlugin, promise);
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
    if (properties != null && !properties.isEmpty()) {
      capabilitiesFlags |= CLIENT_CONNECT_ATTRS;
    }
    if (!useAffectedRows) {
      capabilitiesFlags |= CLIENT_FOUND_ROWS;
    }

    return capabilitiesFlags;
  }
}

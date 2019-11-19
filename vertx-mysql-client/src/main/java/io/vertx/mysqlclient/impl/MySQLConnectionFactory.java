package io.vertx.mysqlclient.impl;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.TrustOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.impl.Connection;

import java.util.Collections;
import java.util.Map;

import static io.vertx.mysqlclient.impl.codec.CapabilitiesFlag.*;

public class MySQLConnectionFactory {
  private final NetClient netClient;
  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private final Map<String, String> connectionAttributes;
  private final String collation;
  private final boolean useAffectedRows;
  private final SslMode sslMode;
  private final Buffer serverRsaPublicKey;
  private final boolean cachePreparedStatements;
  private final int preparedStatementCacheSize;
  private final int preparedStatementCacheSqlLimit;
  private final int initialCapabilitiesFlags;

  public MySQLConnectionFactory(Vertx vertx, MySQLConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.host = options.getHost();
    this.port = options.getPort();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.connectionAttributes = options.getProperties() == null ? null : Collections.unmodifiableMap(options.getProperties());
    String collation;
    if (options.getCollation() != null) {
      // override the collation if configured
      collation = options.getCollation();
    } else {
      String charset = options.getCharset();
      collation = MySQLCollation.getDefaultCollationFromCharsetName(charset);
    }
    this.collation = collation;
    this.useAffectedRows = options.isUseAffectedRows();
    this.sslMode = options.getSslMode();

    // server RSA public key
    Buffer serverRsaPublicKey = null;
    if (options.getServerRsaPublicKeyValue() != null) {
      serverRsaPublicKey = options.getServerRsaPublicKeyValue();
    } else {
      if (options.getServerRsaPublicKeyPath() != null) {
        serverRsaPublicKey = vertx.fileSystem().readFileBlocking(options.getServerRsaPublicKeyPath());
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
    this.preparedStatementCacheSqlLimit = options.getPreparedStatementCacheSqlLimit();

    this.netClient = vertx.createNetClient(netClientOptions);
  }

  // Called by hook
  private void close(Handler<AsyncResult<Void>> completionHandler) {
    netClient.close();
    completionHandler.handle(Future.succeededFuture());
  }

  void close() {
    netClient.close();
  }

  public Future<Connection> connect(ContextInternal context) {
    Future<NetSocket> fut = netClient.connect(port, host);
    return fut
      .map(so -> {
        MySQLSocketConnection mySQLSocketConnection = new MySQLSocketConnection((NetSocketInternal) so, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, context);
        mySQLSocketConnection.init();
        return mySQLSocketConnection;
      })
      .flatMap(conn -> Future.future(p -> {
        conn.sendStartupMessage(username, password, database, collation, serverRsaPublicKey, connectionAttributes, sslMode, initialCapabilitiesFlags, p);
      }));
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

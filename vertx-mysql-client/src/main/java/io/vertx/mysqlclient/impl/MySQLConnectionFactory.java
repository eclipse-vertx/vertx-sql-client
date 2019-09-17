package io.vertx.mysqlclient.impl;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.NetSocketInternal;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.TrustOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.impl.Connection;

import java.util.Collections;
import java.util.Map;

public class MySQLConnectionFactory {
  private final NetClient netClient;
  private final Context context;
  private final boolean registerCloseHook;
  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String database;
  private final Map<String, String> connectionAttributes;
  private final String collation;
  private final SslMode sslMode;
  private final Buffer serverRsaPublicKey;
  private final boolean cachePreparedStatements;
  private final int preparedStatementCacheSize;
  private final int preparedStatementCacheSqlLimit;
  private final Closeable hook;

  public MySQLConnectionFactory(Context context, boolean registerCloseHook, MySQLConnectOptions options) {
    NetClientOptions netClientOptions = new NetClientOptions(options);

    this.context = context;
    this.registerCloseHook = registerCloseHook;
    this.hook = this::close;
    if (registerCloseHook) {
      context.addCloseHook(hook);
    }

    this.host = options.getHost();
    this.port = options.getPort();
    this.username = options.getUser();
    this.password = options.getPassword();
    this.database = options.getDatabase();
    this.connectionAttributes = Collections.unmodifiableMap(options.getProperties());
    String collation;
    if (options.getCollation() != null) {
      // override the collation if configured
      collation = options.getCollation();
    } else {
      String charset = options.getCharset();
      collation = MySQLCollation.getDefaultCollationFromCharsetName(charset);
    }
    this.collation = collation;
    this.sslMode = options.getSslMode();

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
        MySQLSocketConnection conn = new MySQLSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize, preparedStatementCacheSqlLimit, context);
        conn.init();
        conn.sendStartupMessage(username, password, database, collation, serverRsaPublicKey, connectionAttributes, sslMode, handler);
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    netClient.connect(port, host, promise);
  }
}

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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import static io.vertx.mysqlclient.impl.protocol.CapabilitiesFlag.*;

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
        conn.sendStartupMessage(username, password, database, collation, serverRsaPublicKey, connectionAttributes, sslMode, initialCapabilitiesFlags, charsetEncoding, handler);
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    netClient.connect(port, host, promise);
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

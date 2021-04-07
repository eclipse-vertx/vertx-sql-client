package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.NetSocketInternal;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.SqlConnectionFactoryBase;
import net.jpountz.lz4.LZ4Factory;

public class ClickhouseNativeConnectionFactory extends SqlConnectionFactoryBase implements ConnectionFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseNativeConnectionFactory.class);

  private final LZ4Factory lz4Factory;

  ClickhouseNativeConnectionFactory(EventLoopContext context, ClickhouseNativeConnectOptions options) {
    super(context, options);
    this.lz4Factory = lz4FactoryForName(options.getProperties().getOrDefault(ClickhouseConstants.OPTION_COMPRESSOR, "none"));
  }

  private LZ4Factory lz4FactoryForName(String name) {
    if ("lz4_native".equals(name)) {
      return LZ4Factory.nativeInstance();
    } else if ("lz4_fastest".equals(name)) {
      return LZ4Factory.fastestInstance();
    } else if ("lz4_fastest_java".equals(name)) {
      return LZ4Factory.fastestJavaInstance();
    } else if ("lz4_safe".equals(name)) {
      return LZ4Factory.safeInstance();
    } else if ("lz4_unsafe".equals(name)) {
      return LZ4Factory.unsafeInstance();
    }
    if (!"none".equals(name)) {
      LOG.warn("unknown compressor name '" + name + "', ignored");
    }
    return null;
  }

  @Override
  protected void initializeConfiguration(SqlConnectOptions connectOptions) {
  }

  @Override
  protected void configureNetClientOptions(NetClientOptions netClientOptions) {
    netClientOptions.setSsl(false);
  }

  @Override
  protected void doConnectInternal(Promise<Connection> promise) {
    doConnect().flatMap(conn -> {
      ClickhouseNativeSocketConnection socket = (ClickhouseNativeSocketConnection) conn;
      socket.init();
      return Future.<Connection>future(p -> socket.sendStartupMessage(username, password, database, properties, p))
        .map(conn);
    }).onComplete(promise);
  }

  private Future<Connection> doConnect() {
    Future<NetSocket> soFut;
    try {
      soFut = netClient.connect(socketAddress, (String) null);
    } catch (Exception e) {
      // Client is closed
      return context.failedFuture(e);
    }
    return soFut.map(so -> newSocketConnection((NetSocketInternal) so));
  }

  private ClickhouseNativeSocketConnection newSocketConnection(NetSocketInternal socket) {
    return new ClickhouseNativeSocketConnection(socket, cachePreparedStatements, preparedStatementCacheSize,
      preparedStatementCacheSqlFilter, context, lz4Factory);
  }
}

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgClient;
import com.julienviet.pgclient.PgClientOptions;
import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgConnectionPool;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.MessageDecoder;
import com.julienviet.pgclient.codec.encoder.MessageEncoder;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.NetClientBase;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgClientImpl extends NetClientBase<DbConnection> implements PgClient {

  final VertxInternal vertx;
  final String host;
  final int port;
  final String database;
  final String username;
  final String password;
  final int pipeliningLimit;

  public PgClientImpl(Vertx vertx, PgClientOptions options) {
    super((VertxInternal) vertx, options, true);
    this.host = options.getHost();
    this.port = options.getPort();
    this.database = options.getDatabase();
    this.username = options.getUsername();
    this.password = options.getPassword();
    this.pipeliningLimit = options.getPipeliningLimit();
    this.vertx = (VertxInternal) vertx;
  }

  public void connect(Handler<AsyncResult<PgConnection>> completionHandler) {
    doConnect(port, host, null, ar1 -> {
      if (ar1.succeeded()) {
        DbConnection conn = ar1.result();
        conn.init(username, password, database, ar2 -> {
          if (ar2.succeeded()) {
            completionHandler.handle(Future.succeededFuture(new PgConnectionImpl(ar2.result())));
          } else {
            completionHandler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        completionHandler.handle(Future.failedFuture(ar1.cause()));
      }
    });
  }

  @Override
  public SQLClient getConnection(Handler<AsyncResult<SQLConnection>> handler) {
    doConnect(port, host, null, ar1 -> {
      if (ar1.succeeded()) {
        DbConnection conn = ar1.result();
        conn.init(username, password, database, ar2 -> {
          if (ar2.succeeded()) {
            handler.handle(Future.succeededFuture(new PostgresSQLConnection(ar2.result())));
          } else {
            handler.handle(Future.failedFuture(ar2.cause()));
          }
        });
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
    return this;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException("Implement me");
  }

  @Override
  protected DbConnection createConnection(VertxInternal vertxInternal, Channel channel, String s, int i, ContextImpl context, SSLHelper sslHelper, TCPMetrics tcpMetrics) {
    return new DbConnection(this, vertxInternal, channel, context);
  }

  @Override
  public PgConnectionPool createPool(int size) {
    return new PgConnectionPoolImpl(this, size);
  }

  @Override
  protected void handleMsgReceived(DbConnection conn, Object o) {
    conn.handleMessage((Message) o);
  }

  @Override
  protected void initChannel(ChannelPipeline channelPipeline) {
    channelPipeline.addLast(new MessageDecoder());
    channelPipeline.addLast(new MessageEncoder());
  }

  @Override
  protected Object safeObject(Object o, ByteBufAllocator byteBufAllocator) {
    return o;
  }
}

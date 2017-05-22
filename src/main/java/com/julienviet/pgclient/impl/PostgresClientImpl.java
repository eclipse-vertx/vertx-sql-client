package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PostgresClient;
import com.julienviet.pgclient.PostgresClientOptions;
import com.julienviet.pgclient.PostgresConnectionPool;
import com.julienviet.pgclient.codec.encoder.MessageEncoder;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.NetClientBase;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.spi.metrics.TCPMetrics;
import com.julienviet.pgclient.PostgresConnection;
import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.MessageDecoder;
import com.julienviet.pgclient.codec.encoder.message.StartupMessage;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PostgresClientImpl extends NetClientBase<DbConnection> implements PostgresClient {

  final VertxInternal vertx;
  final String host;
  final int port;
  final String database;
  final String username;
  final String password;
  final int pipeliningLimit;

  public PostgresClientImpl(Vertx vertx, PostgresClientOptions options) {
    super((VertxInternal) vertx, options, true);
    this.host = options.getHost();
    this.port = options.getPort();
    this.database = options.getDatabase();
    this.username = options.getUsername();
    this.password = options.getPassword();
    this.pipeliningLimit = options.getPipeliningLimit();
    this.vertx = (VertxInternal) vertx;
  }

  public void connect(Handler<AsyncResult<PostgresConnection>> completionHandler) {
    doConnect(port, host, null, ar1 -> {
      if (ar1.succeeded()) {
        DbConnection conn = ar1.result();
        conn.init(username, database, ar2 -> {
          if (ar2.succeeded()) {
            completionHandler.handle(Future.succeededFuture(new PostgresConnectionImpl(ar2.result())));
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
  protected DbConnection createConnection(VertxInternal vertxInternal, Channel channel, String s, int i, ContextImpl context, SSLHelper sslHelper, TCPMetrics tcpMetrics) {
    return new DbConnection(this, vertxInternal, channel, context);
  }

  @Override
  public PostgresConnectionPool createPool(int size) {
    return new PostgresConnectionPoolImpl(this, size);
  }

  @Override
  protected void handleMsgReceived(DbConnection conn, Object o) {
    conn.handleMessage((Message) o);
  }

  @Override
  protected void initChannel(ChannelPipeline channelPipeline) {
    channelPipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 1, 4, -4, 0, true));
    channelPipeline.addLast(new MessageDecoder());
    channelPipeline.addLast(new MessageEncoder());
  }

  @Override
  protected Object safeObject(Object o, ByteBufAllocator byteBufAllocator) {
    return o;
  }
}

package io.vertx.pgclient.impl;

import com.github.pgasync.impl.message.Message;
import com.github.pgasync.impl.message.StartupMessage;
import com.github.pgasync.impl.netty.ByteBufMessageDecoderExt;
import com.github.pgasync.impl.netty.ByteBufMessageEncoder;
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
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.impl.NetClientBase;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.pgclient.PostgresClient;
import io.vertx.pgclient.PostgresClientOptions;
import io.vertx.pgclient.PostgresConnection;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PostgresClientImpl extends NetClientBase<DbConnection> implements PostgresClient {

  final String host;
  final int port;
  final String database;
  final String username;
  final String password;
  final int pipeliningLimit;

  public PostgresClientImpl(Vertx vertx, PostgresClientOptions options) {
    super((VertxInternal) vertx, new NetClientOptions(), true);
    host = options.getHost();
    port = options.getPort();
    database = options.getDatabase();
    username = options.getUsername();
    password = options.getPassword();
    pipeliningLimit = options.getPipeliningLimit();
  }

  public void connect(Handler<AsyncResult<PostgresConnection>> connectHandler) {
    doConnect(port, host, null, ar -> {
      if (ar.succeeded()) {
        DbConnection conn = ar.result();
        conn.handler = connectHandler;
        conn.writeToChannel(new StartupMessage(username, database));
      } else {
        connectHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  protected DbConnection createConnection(VertxInternal vertxInternal, Channel channel, String s, int i, ContextImpl context, SSLHelper sslHelper, TCPMetrics tcpMetrics) {
    return new DbConnection(this, vertxInternal, channel, context);
  }

  @Override
  protected void handleMsgReceived(DbConnection conn, Object o) {
    conn.handleMessage((Message) o);
  }

  @Override
  protected void initChannel(ChannelPipeline channelPipeline) {
    channelPipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 1, 4, -4, 0, true));
    channelPipeline.addLast(new ByteBufMessageDecoderExt());
    channelPipeline.addLast(new ByteBufMessageEncoder());
  }

  @Override
  protected Object safeObject(Object o, ByteBufAllocator byteBufAllocator) {
    return o;
  }
}

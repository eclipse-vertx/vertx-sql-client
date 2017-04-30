package io.vertx.pgclient.impl;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.impl.PgConnectionPool;
import com.github.pgasync.impl.PgProtocolStream;
import com.github.pgasync.impl.netty.NettyPgProtocolStream;
import io.vertx.core.Context;
import io.vertx.core.impl.ContextInternal;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class VertxPool extends PgConnectionPool {

  private final ContextInternal context;
  private final boolean pipeline;

  public VertxPool(Context context, ConnectionPoolBuilder.PoolProperties properties) {
    super(properties);
    this.pipeline = properties.getUsePipelining();
    this.context = (ContextInternal) context;
  }

  @Override
  protected PgProtocolStream openStream(InetSocketAddress address) {
    return new NettyPgProtocolStream(context.nettyEventLoop(), address, false, pipeline);
  }
}

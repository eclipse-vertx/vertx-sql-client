module io.vertx.sql.client.codec {

  requires io.netty.common;
  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.transport;

  requires io.vertx.core;
  requires io.vertx.core.logging;

  requires io.vertx.sql.client;

  exports io.vertx.sqlclient.codec;
  exports io.vertx.sqlclient.codec.impl to io.vertx.tests.sql.client.codec;

}

module io.vertx.client.sql.db2  {

  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.common;
  requires io.netty.handler;
  requires io.netty.transport;
  requires io.vertx.client.sql;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires java.sql;

  exports io.vertx.db2client;
  exports io.vertx.db2client.spi;

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;


}

module io.vertx.client.sql.pg {

  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.common;
  requires io.netty.handler;
  requires io.netty.transport;
  requires io.vertx.client.sql;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires java.sql;

  requires static com.ongres.scram.client;
  requires static com.ongres.scram.common;

  exports io.vertx.pgclient;
  exports io.vertx.pgclient.spi;
  exports io.vertx.pgclient.data;
  exports io.vertx.pgclient.pubsub;

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;

}

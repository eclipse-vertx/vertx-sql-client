module io.vertx.sql.client.pg {

  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.common;
  requires io.netty.handler;
  requires io.netty.transport;
  requires io.vertx.sql.client;
  requires io.vertx.sql.client.codec;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires java.sql;

  requires static com.ongres.scram.client;
  requires static com.ongres.scram.common;

  provides io.vertx.sqlclient.spi.Driver with io.vertx.pgclient.spi.PgDriver;

  exports io.vertx.pgclient;
  exports io.vertx.pgclient.spi;
  exports io.vertx.pgclient.data;
  exports io.vertx.pgclient.pubsub;

  exports io.vertx.pgclient.impl to io.vertx.tests.sql.client.pg;
  exports io.vertx.pgclient.impl.util to io.vertx.tests.sql.client.pg;
  exports io.vertx.pgclient.impl.codec to io.vertx.tests.sql.client.pg;
  exports io.vertx.pgclient.impl.pubsub to io.vertx.tests.sql.client.pg;

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;

}

import io.vertx.core.spi.VertxServiceProvider;
import io.vertx.sqlclient.impl.TransactionPropagationLocal;

module io.vertx.sql.client {

  requires io.netty.common;
  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.transport;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires java.sql;

  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;

  exports io.vertx.sqlclient;
  exports io.vertx.sqlclient.data;
  exports io.vertx.sqlclient.desc;
  exports io.vertx.sqlclient.spi;

  uses io.vertx.sqlclient.spi.Driver;

  provides io.vertx.core.spi.VertxServiceProvider with io.vertx.sqlclient.impl.TransactionPropagationLocal;

  // Expose enough for implementing a client back-end on top of this API (e.g. vertx-jdbc-client)

  exports io.vertx.sqlclient.internal;
  exports io.vertx.sqlclient.internal.command;
  exports io.vertx.sqlclient.internal.pool;

  // Expose impl to our own implementation, this actually would deserve to be in another module since it is not
  // related to the API or the internal API

  exports io.vertx.sqlclient.impl to
    io.vertx.tests.sql.client, io.vertx.tests.sql.client.pg, io.vertx.tests.sql.client.mysql, io.vertx.tests.sql.client.templates,
    io.vertx.sql.client.pg, io.vertx.sql.client.mysql, io.vertx.sql.client.mssql, io.vertx.sql.client.db2, io.vertx.sql.client.oracle, io.vertx.sql.client.templates;
  exports io.vertx.sqlclient.impl.codec to io.vertx.sql.client.pg, io.vertx.tests.sql.client.pg, io.vertx.sql.client.mysql, io.vertx.sql.client.mssql, io.vertx.sql.client.db2;
  exports io.vertx.sqlclient.impl.cache to io.vertx.tests.sql.client, io.vertx.sql.client.mysql, io.vertx.sql.client.mssql, io.vertx.sql.client.db2;
  exports io.vertx.sqlclient.impl.tracing to io.vertx.tests.sql.client;
  exports io.vertx.sqlclient.impl.pool;

}

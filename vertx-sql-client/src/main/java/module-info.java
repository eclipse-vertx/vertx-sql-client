module io.vertx.client.sql {

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

  // Expose enough for implementing a client back-end on top of this API (e.g. vertx-jdbc-client)

  exports io.vertx.sqlclient.internal;
  exports io.vertx.sqlclient.internal.command;
  exports io.vertx.sqlclient.internal.pool;

  // Expose impl to our own implementation, this actually would deserve to be in another module since it is not
  // related to the API or the internal API

  exports io.vertx.sqlclient.impl to io.vertx.client.sql.pg, io.vertx.client.sql.mysql, io.vertx.client.sql.mssql, io.vertx.client.sql.db2, io.vertx.client.sql.templates;
  exports io.vertx.sqlclient.impl.codec to io.vertx.client.sql.pg, io.vertx.client.sql.mysql, io.vertx.client.sql.mssql, io.vertx.client.sql.db2;
  exports io.vertx.sqlclient.impl.cache to io.vertx.client.sql.mysql, io.vertx.client.sql.mssql, io.vertx.client.sql.db2;
  exports io.vertx.sqlclient.impl.pool;

}

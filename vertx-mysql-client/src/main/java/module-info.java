module io.vertx.sql.client.mysql {

  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.common;
  requires io.netty.handler;
  requires io.netty.transport;
  requires io.vertx.sql.client;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires java.sql;

  provides io.vertx.sqlclient.spi.Driver with io.vertx.mysqlclient.spi.MySQLDriver;

  exports io.vertx.mysqlclient;
  exports io.vertx.mysqlclient.spi;
  exports io.vertx.mysqlclient.data.spatial;

  exports io.vertx.mysqlclient.impl to io.vertx.tests.sql.client.mysql;
  exports io.vertx.mysqlclient.impl.util to io.vertx.tests.sql.client.mysql;
  exports io.vertx.mysqlclient.impl.protocol to io.vertx.tests.sql.client.mysql;

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;


}

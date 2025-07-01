module io.vertx.sql.client.mssql {

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

  provides io.vertx.sqlclient.spi.Driver with io.vertx.mssqlclient.spi.MSSQLDriver;

  exports io.vertx.mssqlclient;
  exports io.vertx.mssqlclient.spi;

  exports io.vertx.mssqlclient.impl to io.vertx.tests.sql.client.mssql;

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;


}

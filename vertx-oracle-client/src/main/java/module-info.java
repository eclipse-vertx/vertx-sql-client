module io.vertx.sql.client.oracle {

  requires io.netty.buffer;
  requires io.netty.codec;
  requires io.netty.common;
  requires io.netty.handler;
  requires io.netty.transport;
  requires com.oracle.database.jdbc;
  requires io.vertx.sql.client;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires java.sql;

  exports io.vertx.oracleclient;
  exports io.vertx.oracleclient.data;
  exports io.vertx.oracleclient.spi;

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;


}

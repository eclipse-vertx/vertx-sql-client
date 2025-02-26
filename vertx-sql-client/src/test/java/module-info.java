open module io.vertx.tests.sql.client {

  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires io.vertx.sql.client;
  requires io.vertx.testing.unit;
  requires java.sql;
  requires junit;

  uses io.vertx.sqlclient.spi.Driver;

  exports io.vertx.tests.sqlclient;
  exports io.vertx.tests.sqlclient.tck;

}

open module io.vertx.tests.sql.client.mssql {

  requires io.vertx.core;
  requires io.vertx.sql.client;
  requires io.vertx.sql.client.mssql;
  requires io.vertx.tests.sql.client;
  requires java.sql;
  requires io.vertx.testing.unit;
  requires junit;
  requires testcontainers;
  requires hamcrest.core;

}

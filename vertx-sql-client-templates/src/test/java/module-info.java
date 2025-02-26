open module io.vertx.tests.sql.client.templates {
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires io.vertx.codegen.api;
  requires io.vertx.core;
  requires io.vertx.sql.client;
  requires io.vertx.sql.client.mysql;
  requires io.vertx.sql.client.pg;
  requires io.vertx.sql.client.templates;
  requires io.vertx.testing.unit;
  requires junit;
  requires testcontainers;
}

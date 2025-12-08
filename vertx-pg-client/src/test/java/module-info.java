open module io.vertx.tests.sql.client.pg {

  requires io.netty.buffer;
  requires io.netty.transport;
  requires io.vertx.core;
  requires io.vertx.sql.client;
  requires io.vertx.sql.client.pg;
  requires io.vertx.tests.sql.client;
  requires java.sql;
  requires jmh.core;
  requires io.vertx.testing.unit;
  requires junit;
  requires testcontainers;
  requires static org.slf4j;
  requires org.apache.commons.compress;

}

package io.vertx.sqlclient.template;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnectOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;

@RunWith(VertxUnitRunner.class)
public abstract class TemplateTestBase {

  private static PostgreSQLContainer server;

  @BeforeClass
  public static void startDatabase() {
    server = new PostgreSQLContainer("postgres:" + "10.10")
      .withDatabaseName("postgres")
      .withUsername("postgres")
      .withPassword("postgres");
    server.start();
  }

  @AfterClass
  public static void stopDatabase() {
    try {
      server.stop();
    } finally {
      server = null;
    }
  }

  public static PgConnectOptions connectOptions() {
    Integer port = server.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);
    String ip = server.getContainerIpAddress();
    return new PgConnectOptions()
      .setPort(port)
      .setHost(ip)
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres");
  }
}

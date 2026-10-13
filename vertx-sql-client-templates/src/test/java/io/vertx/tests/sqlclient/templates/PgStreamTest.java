package io.vertx.tests.sqlclient.templates;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.SqlConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static io.vertx.pgclient.PgConnectOptions.DEFAULT_PORT;

public class PgStreamTest extends StreamTestBase {

  private static GenericContainer<?> server;

  @BeforeClass
  public static void startDatabase() {
    server = new GenericContainer<>("postgres:10.10")
      .withEnv("POSTGRES_DB", "postgres")
      .withEnv("POSTGRES_USER", "postgres")
      .withEnv("POSTGRES_PASSWORD", "postgres")
      .withExposedPorts(DEFAULT_PORT)
      .waitingFor(new LogMessageWaitStrategy()
        .withRegEx(".*database system is ready to accept connections.*\\s")
        .withTimes(2)
        .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS)))
      .withCommand("postgres", "-c", "fsync=off");
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

  private static PgConnectOptions connectOptions() {
    return new PgConnectOptions()
      .setPort(server.getMappedPort(DEFAULT_PORT))
      .setHost(server.getHost())
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres");
  }

  @Override
  protected Future<SqlConnection> connect(Vertx vertx) {
    return PgConnection.connect(vertx, connectOptions()).map(conn -> conn);
  }

  @Override
  protected String createTableSql() {
    return "CREATE TABLE test_stream (id INT4)";
  }

  @Override
  protected String selectSingleRowSql() {
    return "SELECT #{id} :: INT4 \"id\"";
  }

  @Override
  protected String selectTwoColumnsSql() {
    return "SELECT #{id} :: INT4 \"id\", #{randomnumber} :: INT4 \"randomnumber\"";
  }
}

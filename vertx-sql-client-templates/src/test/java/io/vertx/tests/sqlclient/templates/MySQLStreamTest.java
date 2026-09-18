package io.vertx.tests.sqlclient.templates;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.SqlConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.containers.GenericContainer;

public class MySQLStreamTest extends StreamTestBase {

  private static GenericContainer<?> server;

  @BeforeClass
  public static void startDatabase() {
    server = new GenericContainer<>("mysql:8.0")
      .withEnv("MYSQL_USER", "mysql")
      .withEnv("MYSQL_PASSWORD", "password")
      .withEnv("MYSQL_ROOT_PASSWORD", "password")
      .withEnv("MYSQL_DATABASE", "testschema")
      .withExposedPorts(3306)
      .withReuse(true);
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

  private static MySQLConnectOptions connectOptions() {
    return new MySQLConnectOptions()
      .setPort(server.getMappedPort(3306))
      .setHost(server.getHost())
      .setDatabase("testschema")
      .setUser("mysql")
      .setPassword("password");
  }

  @Override
  protected Future<SqlConnection> connect(Vertx vertx) {
    return MySQLConnection.connect(vertx, connectOptions()).map(conn -> conn);
  }

  @Override
  protected String createTableSql() {
    return "CREATE TABLE test_stream (id INT)";
  }

  @Override
  protected String selectSingleRowSql() {
    return "SELECT CAST(#{id} AS SIGNED) AS id";
  }

  @Override
  protected String selectTwoColumnsSql() {
    return "SELECT CAST(#{id} AS SIGNED) AS id, CAST(#{randomnumber} AS SIGNED) AS randomnumber";
  }
}

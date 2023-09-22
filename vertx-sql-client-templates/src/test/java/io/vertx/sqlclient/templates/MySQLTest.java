package io.vertx.sqlclient.templates;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;

@RunWith(VertxUnitRunner.class)
public class MySQLTest {

  private static GenericContainer server;

  @BeforeClass
  public static void startDatabase() {
    server = new GenericContainer("mysql:8.0")
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

  public static MySQLConnectOptions connectOptions() {
    Integer port = server.getMappedPort(3306);
    String ip = server.getHost();
    return new MySQLConnectOptions()
      .setPort(port)
      .setHost(ip)
      .setDatabase("testschema")
      .setUser("mysql")
      .setPassword("password");
  }

  protected Vertx vertx;
  protected Pool pool;

  @Before
  public void setup() throws Exception {
    vertx = Vertx.vertx();
    pool = MySQLBuilder.pool(builder -> builder.connectingTo(connectOptions()).using(vertx));
  }

  @Test
  public void testDurationMapping(TestContext ctx) {
    Duration duration = Duration.ofHours(11);
    SqlTemplate
      .forQuery(pool, "SELECT CAST(#{duration} AS TIME) AS duration")
      .mapFrom(MySQLDataObjectParametersMapper.INSTANCE)
      .mapTo(MySQLDataObjectRowMapper.INSTANCE)
      .execute(new MySQLDataObject().setDuration(duration))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        MySQLDataObject row = rows.iterator().next();
        ctx.assertEquals(duration, row.getDuration());
      }));
  }
}

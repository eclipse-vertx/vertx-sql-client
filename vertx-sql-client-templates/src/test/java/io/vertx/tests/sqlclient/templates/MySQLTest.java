package io.vertx.tests.sqlclient.templates;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@RunWith(VertxUnitRunner.class)
public class MySQLTest {

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

  @Test
  public void executeWithOtherClient(TestContext ctx) {
    SqlTemplate<Map<String, Object>, RowSet<World>> template = SqlTemplate
      .forQuery(pool, "SELECT id, randomnumber FROM tmp_world")
      .mapTo(World.class);

    pool.withTransaction(conn -> {
      // Create a table visible only within the current session
      return conn.query("CREATE TEMPORARY TABLE tmp_world (" +
          "id int(10) unsigned NOT NULL auto_increment, " +
          "randomnumber int NOT NULL default 0, " +
          "PRIMARY KEY (id)) " +
          "ENGINE=INNODB")
        .execute()
        .compose(v -> {
          return conn.query("INSERT INTO tmp_world (randomnumber) VALUES " +
              "(floor(0 + (rand() * 10000))), " +
              "(floor(0 + (rand() * 10000))), " +
              "(floor(0 + (rand() * 10000)))")
            .execute();
        })
        .compose(v -> {
          return template.withClient(conn).execute(Collections.emptyMap());
        });
    }).onComplete(ctx.asyncAssertSuccess(rows -> {
      ctx.assertEquals(3, rows.size());
      for (World world : rows) {
        ctx.assertNotNull(world.id);
      }
    }));
  }
}

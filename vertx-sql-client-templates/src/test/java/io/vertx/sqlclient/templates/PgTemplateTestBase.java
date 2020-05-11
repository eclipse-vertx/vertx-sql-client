package io.vertx.sqlclient.templates;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public abstract class PgTemplateTestBase {

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

  protected Vertx vertx;
  protected PgConnection connection;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    Async async = ctx.async();
    PgConnection.connect(vertx, connectOptions(), ctx.asyncAssertSuccess(conn -> {
      connection = conn;
      async.complete();
    }));
    async.await(10000);
  }

  @After
  public void teardown(TestContext ctx) {
    if (connection != null) {
      connection.close();
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected <P, T, V> void testGet(TestContext ctx,
                                   String sqlType,
                                   Function<Row, T> rowMapper,
                                   Function<P, Map<String, Object>> paramsMapper,
                                   String paramName,
                                   P params,
                                   V expected,
                                   Function<T, V> extractor,
                                   String column) {
    Async async = ctx.async();
    SqlTemplate<P, RowSet<T>> template = SqlTemplate
      .forQuery(connection, "SELECT ${" + paramName + "} :: " + sqlType + " \"" + column + "\"")
      .mapFrom(paramsMapper)
      .mapTo(rowMapper);
    template.execute(params, ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      ctx.assertEquals(expected, extractor.apply(result.iterator().next()));
      async.complete();
    }));
    async.await(10000);
  }
}

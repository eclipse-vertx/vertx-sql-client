package io.vertx.pgclient;


import io.vertx.core.*;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class PgBouncerTest {

  private FixedHostPortGenericContainer<?> pgContainer;
  private GenericContainer<?> pgBouncerContainer;
  private Vertx vertx;
  private PgConnectOptions options;

  @Before
  public void setUp() {
    pgContainer = new FixedHostPortGenericContainer<>("postgres:10.10")
      .withFixedExposedPort(5432, 5432);
    pgContainer.withEnv("POSTGRES_PASSWORD", "postgres");
    pgContainer.withEnv("POSTGRES_USER", "postgres");
    pgContainer.withEnv("POSTGRES_DB", "postgres");
    pgContainer.withExposedPorts(5432);
    pgContainer.withNetworkAliases("foo");
    pgContainer.start();
    Integer pgPort = pgContainer.getFirstMappedPort();
    String pgHost = pgContainer.getHost();

    Testcontainers.exposeHostPorts(pgPort);


    pgBouncerContainer = new GenericContainer(DockerImageName.parse("bitnamilegacy/pgbouncer:1.20.1-debian-11-r30"));
    pgBouncerContainer.withEnv("POSTGRESQL_USERNAME", "postgres");
    pgBouncerContainer.withEnv("POSTGRESQL_PASSWORD", "postgres");
    pgBouncerContainer.withEnv("POSTGRESQL_DATABASE", "postgres");
    pgBouncerContainer.withEnv("POSTGRESQL_HOST", "host.testcontainers.internal");
    pgBouncerContainer.withEnv("POSTGRESQL_PORT", "" + pgPort);
    pgBouncerContainer.withEnv("PGBOUNCER_IGNORE_STARTUP_PARAMETERS", "extra_float_digits");
    pgBouncerContainer.withEnv("PGBOUNCER_POOL_MODE", "transaction");
    pgBouncerContainer.withEnv("PGBOUNCER_MAX_DB_CONNECTIONS", "2");
    pgBouncerContainer.withExposedPorts(6432);
    pgBouncerContainer.start();
    Integer bouncerPort = pgBouncerContainer.getFirstMappedPort();
    String bouncerHost = pgBouncerContainer.getHost();

    options = new PgConnectOptions()
      .setHost(bouncerHost)
      .setPort(bouncerPort)
      .setUser("postgres")
      .setPassword("postgres")
      .setDatabase("postgres")
      .addProperty("application_name", "pgbouncer-test")
      .setPipeliningLimit(1);

    vertx = Vertx.vertx();
  }

  @After
  public void tearDown() throws Exception {
    Testcontainers.exposeHostPorts();
    pgBouncerContainer.stop();
    pgContainer.stop();
    vertx.close().toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
  }

  @Test
  public void testPreparedQuery() throws Exception {
    List<PgConnection> connections = new ArrayList<>();
    int numConn = 2;
    for (int i = 0;i < numConn;i++) {
      connections.add(PgConnection.connect(vertx, new PgConnectOptions(options).setUseLayer7Proxy(true)).toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS));
    }
    CompositeFuture cf = CompositeFuture.join(connections.stream()
      .map(conn -> conn.preparedQuery("select 1").execute().map(rows -> rows.iterator().next().getInteger(0)))
      .collect(Collectors.toList()));
    cf.toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
    for (int i = 0;i < numConn;i++) {
      assertEquals(1, (cf.<Object>resultAt(i)));
    }
  }

  @Test
  public void testPreparedBatch() throws Exception {
    List<PgConnection> connections = new ArrayList<>();
    int numConn = 2;
    for (int i = 0;i < numConn;i++) {
      connections.add(PgConnection.connect(vertx, new PgConnectOptions(options).setUseLayer7Proxy(true)).toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS));
    }
    CompositeFuture cf = CompositeFuture.join(connections.stream()
      .map(conn -> conn.preparedQuery("select 1")
        .executeBatch(Arrays.asList(Tuple.tuple(), Tuple.tuple()))
        .map(rows -> rows.iterator().next().getInteger(0)))
      .collect(Collectors.toList()));
    cf.toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
    for (int i = 0;i < numConn;i++) {
      assertEquals(1, (cf.<Object>resultAt(i)));
    }
  }

  @Test
  public void testCursor() throws Exception {
    List<PgConnection> connections = new ArrayList<>();
    int numConn = 2;
    for (int i = 0;i < numConn;i++) {
      connections.add(PgConnection.connect(vertx, new PgConnectOptions(options).setUseLayer7Proxy(true)).toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS));
    }
    List<Future> list = new ArrayList<>();
    for (int i = 0;i < numConn;i++) {
      int val = i;
      PgConnection conn = connections.get(i);
      list.add(conn
        .begin()
        .compose(tx -> conn
          .prepare("select " + val)
          .compose(ps -> {
            Cursor cursor = ps.cursor();
            return cursor
              .read(10)
              .map(res -> res.iterator().next().getInteger(0))
              .eventually(v -> cursor.close())
              .eventually(v -> ps.close());
      }).eventually(v -> tx.commit())));
    }
    CompositeFuture cf = CompositeFuture.join(list);
    cf.toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
    for (int i = 0;i < numConn;i++) {
      assertEquals(i, (cf.<Object>resultAt(i)));
    }
  }
}

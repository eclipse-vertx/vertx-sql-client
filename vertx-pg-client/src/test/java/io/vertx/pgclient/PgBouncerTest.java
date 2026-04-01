/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.pgclient;


import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.testcontainers.containers.BindMode.READ_ONLY;

@RunWith(VertxUnitRunner.class)
public class PgBouncerTest {

  @Rule
  public Timeout timeout = new Timeout(5, TimeUnit.MINUTES);

  private FixedHostPortGenericContainer<?> pgContainer;
  private GenericContainer<?> pgBouncerContainer;
  private Vertx vertx;
  private PgConnectOptions options;
  private List<PgConnection> connections = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    pgContainer = new FixedHostPortGenericContainer<>("postgres:10.10")
      .withFixedExposedPort(5432, 5432);
    pgContainer.withEnv("POSTGRES_PASSWORD", "postgres");
    pgContainer.withEnv("POSTGRES_USER", "postgres");
    pgContainer.withEnv("POSTGRES_DB", "postgres");
    pgContainer.withExposedPorts(5432);
    pgContainer.withNetworkAliases("foo");
    pgContainer.start();
    Integer pgPort = pgContainer.getFirstMappedPort();

    Testcontainers.exposeHostPorts(pgPort);

    pgBouncerContainer = new GenericContainer<>(
      new ImageFromDockerfile("vertx-pgclient-pg-bouncer", false)
        .withFileFromClasspath("Dockerfile", "pgBouncer/Dockerfile"))
      .withClasspathResourceMapping("pgBouncer/pgbouncer.ini", "/etc/pgbouncer/pgbouncer.ini", READ_ONLY)
      .withClasspathResourceMapping("pgBouncer/userlist.txt", "/etc/pgbouncer/userlist.txt", READ_ONLY)
      .withExposedPorts(6432);
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

    // Initialize connections for tests
    int numConn = 2;
    for (int i = 0; i < numConn; i++) {
      connections.add(Future.await(PgConnection.connect(vertx, new PgConnectOptions(options).setUseLayer7Proxy(true)), 20, TimeUnit.SECONDS));
    }
  }

  @After
  public void tearDown() throws Exception {
    for (PgConnection conn : connections) {
      Future.await(conn.close(), 20, TimeUnit.SECONDS);
    }
    Testcontainers.exposeHostPorts();
    pgBouncerContainer.stop();
    pgContainer.stop();
    Future.await(vertx.close(), 20, TimeUnit.SECONDS);
  }

  @Test
  public void testPreparedQuery() throws Exception {
    CompositeFuture cf = CompositeFuture.join(connections.stream()
      .map(conn -> conn.preparedQuery("select 1").execute().map(rows -> rows.iterator().next().getInteger(0)))
      .collect(Collectors.toList()));
    Future.await(cf, 20, TimeUnit.SECONDS);
    for (int i = 0; i < connections.size(); i++) {
      assertEquals(1, (cf.<Object>resultAt(i)));
    }
  }

  @Test
  public void testPreparedBatch() throws Exception {
    CompositeFuture cf = CompositeFuture.join(connections.stream()
      .map(conn -> conn.preparedQuery("select 1")
        .executeBatch(Arrays.asList(Tuple.tuple(), Tuple.tuple()))
        .map(rows -> rows.iterator().next().getInteger(0)))
      .collect(Collectors.toList()));
    Future.await(cf, 20, TimeUnit.SECONDS);
    for (int i = 0; i < connections.size(); i++) {
      assertEquals(1, (cf.<Object>resultAt(i)));
    }
  }

  @Test
  public void testCursor() throws Exception {
    List<Future> list = new ArrayList<>();
    for (int i = 0; i < connections.size(); i++) {
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
    Future.await(cf, 20, TimeUnit.SECONDS);
    for (int i = 0; i < connections.size(); i++) {
      assertEquals(i, (cf.<Object>resultAt(i)));
    }
  }
}

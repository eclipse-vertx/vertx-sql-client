/*
 * Copyright (C) 2018 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.reactiverse.pgclient.it;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

@RunWith(VertxUnitRunner.class)
public class UnixDomainSocketTest {
  private static final String PG_HOST = "/var/run/postgresql";
  private static final String PG_DATABASE = "postgres";
  private static final String PG_USER = "postgres";
  private static final String PG_PASSWORD = "postgres";

  @ClassRule
  public static GenericContainer postgres = new GenericContainer("postgres:latest")
    .withFileSystemBind(PG_HOST, PG_HOST, BindMode.READ_WRITE)
    .withEnv("POSTGRES_USER", PG_USER)
    .withEnv("POSTGRES_PASSWORD", PG_PASSWORD)
    .withEnv("POSTGRES_DB", PG_DATABASE);

  private PgPoolOptions options;
  private PgPool client;

  @Test
  public void envTest(TestContext context) {
    Async async = context.async();
    options = PgPoolOptions.fromEnv();
    client = PgClient.pool(options);
    client.getConnection(connection -> {
      context.assertTrue(connection.succeeded());
      async.complete();
    });
  }

  @Test
  public void uriTest(TestContext context) {
    Async async = context.async();
    String uri = "postgresql://postgres:postgres@/postgres?host=/var/run/postgresql";
    client = PgClient.pool(uri);
    client.getConnection(connection -> {
      context.assertTrue(connection.succeeded());
      async.complete();
    });
  }

  @Test
  public void simpleConnect(TestContext context) {
    Async async = context.async();
    options = new PgPoolOptions()
      .setDomainSocket(true)
      .setHost(PG_HOST)
      .setDatabase(PG_DATABASE)
      .setUser(PG_USER)
      .setPassword(PG_PASSWORD);
    client = PgClient.pool(options);
    client.getConnection(connection -> {
      context.assertTrue(connection.succeeded());
      async.complete();
    });
  }

  @Test
  public void connectWithVertxInstance(TestContext context) {
    Async async = context.async();
    VertxOptions vertxOptions = new VertxOptions().setPreferNativeTransport(true);
    Vertx vertx = Vertx.vertx(vertxOptions);
    options = new PgPoolOptions()
      .setDomainSocket(true)
      .setHost(PG_HOST)
      .setDatabase(PG_DATABASE)
      .setUser(PG_USER)
      .setPassword(PG_PASSWORD);
    client = PgClient.pool(vertx, options);
    client.getConnection(connection -> {
      context.assertTrue(connection.succeeded());
      async.complete();
    });
  }
}

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
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class UnixDomainSocketTest {
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
      .setHost("/var/run/postgresql")
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres");
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
      .setHost("/var/run/postgresql")
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres");
    client = PgClient.pool(vertx, options);
    client.getConnection(connection -> {
      context.assertTrue(connection.succeeded());
      async.complete();
    });
  }
}

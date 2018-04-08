/*
 * Copyright (C) 2017 Julien Viet
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

package io.reactiverse.pgclient;

import io.vertx.core.Vertx;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.CountDownLatch;

@State(Scope.Thread)
public abstract class PgBenchmarkBase extends BenchmarkBase {

  @Param("localhost")
  String host;

  @Param("8081")
  int port;

  @Param("postgres")
  String database;

  @Param("postgres")
  String username;

  @Param("postgres")
  String password;

  @Param("1")
  int pipeliningLimit;

  Vertx vertx;
  PgPool pool;

  @Setup
  public void setup() throws Exception {
    vertx = Vertx.vertx();
    pool = PgClient.pool(vertx, new PgPoolOptions()
      .setHost(host)
      .setPort(port)
      .setDatabase(database)
      .setUser(username)
      .setPassword(password)
      .setCachePreparedStatements(true)
      .setPipeliningLimit(pipeliningLimit)
      .setMaxSize(1));
  }

  @TearDown
  public void tearDown() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    vertx.close(ar -> {
      latch.countDown();
    });
    latch.await();
  }
}

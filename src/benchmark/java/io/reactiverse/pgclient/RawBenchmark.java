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
import org.postgresql.PGProperty;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class RawBenchmark {

  private static final Tuple args = Tuple.of(1);

  public static void main(String[] args) throws Exception {
    PgConnectOptions options = PgTestBase.startPg();
    /*
    PgConnectOptions options = new PgConnectOptions()
      .setHost("localhost")
      .setPort(5432)
      .setDatabase("postgres")
      .setUser("postgres")
      .setPassword("postgres");
    */
    largeSelectJDBC(options, 5_000);
    largeSelect(options, 5_000);
    singleSelectJDBC(options, 200_000);
    singleSelect(options, 200_000);
  }

  interface Benchmark {

    void run(Connection conn) throws Exception;

  }

  private static void singleSelectJDBC(PgConnectOptions options, int reps) throws Exception {
    benchmark("Single select jdbc", options, conn -> {
      PreparedStatement ps = conn.prepareStatement("select id, randomnumber from WORLD where id=(?)");
      for (int i = 0;i < reps;i++) {
        ps.setInt(1, 1);
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()) {
          resultSet.getInt(1);
        }
        resultSet.close();
      }
    });
  }

  private static void largeSelectJDBC(PgConnectOptions options, int reps) throws Exception {
    benchmark("Large select jdbc", options, conn -> {
      PreparedStatement ps = conn.prepareStatement("SELECT id, randomnumber from WORLD");
      for (int i = 0;i < reps;i++) {
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()) {
          resultSet.getInt(1);
        }
        resultSet.close();
      }
    });
  }

  private static void benchmark(String name, PgConnectOptions options, Benchmark benchmark) throws Exception {
    Properties props = new Properties();
    PGProperty.PREPARE_THRESHOLD.set(props, -1);
    PGProperty.BINARY_TRANSFER.set(props, "true");
    // PGProperty.BINARY_TRANSFER_ENABLE.set(props, "true");
    PGProperty.USER.set(props, "postgres");
    PGProperty.PASSWORD.set(props, "postgres");
    Connection conn = DriverManager.getConnection("jdbc:postgresql://"
      + options.getHost() + ":"
      + options.getPort() + "/postgres", props);
    long now = System.currentTimeMillis();
    benchmark.run(conn);
    System.out.println(name + ": " + (System.currentTimeMillis() - now));
  }

  private static void singleSelect(PgConnectOptions options, int reps) throws Exception {
    benchmark("Single select", options, (conn, latch) -> doSingleQuery(conn, reps, latch));
  }

  private static void largeSelect(PgConnectOptions options, int reps) throws Exception {
    benchmark("Large select", options, (conn, latch) -> doLargeQuery(conn, reps, latch));
  }

  private static void doSingleQuery(PgConnection conn, int remaining, CompletableFuture<Void> latch) {
    if (remaining > 0) {
      conn.preparedQuery("SELECT id, randomnumber from WORLD where id=$1", args, ar -> {
        if (ar.succeeded()) {
          doSingleQuery(conn, remaining -1, latch);
        } else {
          latch.completeExceptionally(ar.cause());
        }
      });
    } else {
      latch.complete(null);
    }
  }

  private static void doLargeQuery(PgConnection conn, int remaining, CompletableFuture<Void> latch) {
    if (remaining > 0) {
      conn.preparedQuery("SELECT id, randomnumber from WORLD", ar -> {
        if (ar.succeeded()) {
          doLargeQuery(conn, remaining -1, latch);
          PgRowSet result = ar.result();
          for (Tuple tuple : result) {
            int val = tuple.getInteger(0);
          }
        } else {
          latch.completeExceptionally(ar.cause());
        }
      });
    } else {
      latch.complete(null);
    }
  }

  private static void benchmark(String name, PgConnectOptions options, BiConsumer<PgConnection, CompletableFuture<Void>> benchmark) throws Exception {
    Vertx vertx = Vertx.vertx();
    PgPool client = PgClient.pool(vertx, new PgPoolOptions()
      .setHost(options.getHost())
      .setPort(options.getPort())
      .setDatabase(options.getDatabase())
      .setUser(options.getUser())
      .setPassword(options.getPassword())
      .setCachePreparedStatements(true)
    );
    CompletableFuture<Void> latch = new CompletableFuture<>();
    long now = System.currentTimeMillis();
    client.getConnection(ar -> {
      if (ar.succeeded()) {
        benchmark.accept(ar.result(), latch);
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    latch.get(2, TimeUnit.MINUTES);
    System.out.println(name + ": " + (System.currentTimeMillis() - now));
  }
}

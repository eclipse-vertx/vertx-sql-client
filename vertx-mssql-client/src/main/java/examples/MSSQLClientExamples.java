/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package examples;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLPool;
import io.vertx.core.Vertx;
import io.vertx.docgen.Source;
import io.vertx.sqlclient.*;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Source
public class MSSQLClientExamples {
  public void gettingStarted() {

    // Connect options
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the client pool
    MSSQLPool client = MSSQLPool.pool(connectOptions, poolOptions);

    // A simple query
    client
      .query("SELECT * FROM users WHERE id='julien'")
      .execute(ar -> {
      if (ar.succeeded()) {
        RowSet result = ar.result();
        System.out.println("Got " + result.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }

      // Now close the pool
      client.close();
    });
  }

  public void configureFromDataObject(Vertx vertx) {

    // Data object
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool Options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pool from the data object
    MSSQLPool pool = MSSQLPool.pool(vertx, connectOptions, poolOptions);

    pool.getConnection(ar -> {
      // Handling your connection
    });
  }

  public void connecting01() {

    // Connect options
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MSSQLPool client = MSSQLPool.pool(connectOptions, poolOptions);
  }


  public void connecting02(Vertx vertx) {

    // Connect options
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    // Create the pooled client
    MSSQLPool client = MSSQLPool.pool(vertx, connectOptions, poolOptions);
  }

  public void connecting03(Pool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Connect options
    MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
      .setPort(1433)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MSSQLPool client = MSSQLPool.pool(vertx, connectOptions, poolOptions);

    // Get a connection from the pool
    client.getConnection().compose(conn -> {
      System.out.println("Got a connection from the pool");

      // All operations execute on the same connection
      return conn
        .query("SELECT * FROM users WHERE id='julien'")
        .execute()
        .compose(res -> conn
          .query("SELECT * FROM users WHERE id='emad'")
          .execute())
        .onComplete(ar -> {
          // Release the connection to the pool
          conn.close();
        });
    }).onComplete(ar -> {
      if (ar.succeeded()) {

        System.out.println("Done");
      } else {
        System.out.println("Something went wrong " + ar.cause().getMessage());
      }
    });
  }

  public void reconnectAttempts(MSSQLConnectOptions options) {
    // The client will try to connect at most 3 times at a 1 second interval
    options
      .setReconnectAttempts(2)
      .setReconnectInterval(1000);
  }

  public void collector01Example(SqlClient client) {

    // Create a collector projecting a row set to a map
    Collector<Row, ?, Map<Long, String>> collector = Collectors.toMap(
      row -> row.getLong("id"),
      row -> row.getString("last_name"));

    // Run the query with the collector
    client.query("SELECT * FROM users")
      .collecting(collector)
      .execute(ar -> {
        if (ar.succeeded()) {
          SqlResult<Map<Long, String>> result = ar.result();

          // Get the map created by the collector
          Map<Long, String> map = result.value();
          System.out.println("Got " + map);
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  public void collector02Example(SqlClient client) {

    // Create a collector projecting a row set to a (last_name_1,last_name_2,...)
    Collector<Row, ?, String> collector = Collectors.mapping(
      row -> row.getString("last_name"),
      Collectors.joining(",", "(", ")")
    );

    // Run the query with the collector
    client.query("SELECT * FROM users")
      .collecting(collector)
      .execute(ar -> {
        if (ar.succeeded()) {
          SqlResult<String> result = ar.result();

          // Get the string created by the collector
          String list = result.value();
          System.out.println("Got " + list);
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  enum Color {
    red, green, blue
  }

  public void enumeratedType01Example(SqlClient client) {
    client
      .preparedQuery("INSERT INTO colors VALUES (@p1)")
      .execute(Tuple.of(Color.red),  res -> {
        // ...
      });
  }

  public void enumeratedType02Example(SqlClient client) {
    client
      .preparedQuery("SELECT color FROM colors")
      .execute()
      .onComplete(res -> {
        if (res.succeeded()) {
          RowSet<Row> rows = res.result();
          for (Row row : rows) {
            System.out.println(row.get(Color.class, "color"));
          }
        }
      });
  }
}

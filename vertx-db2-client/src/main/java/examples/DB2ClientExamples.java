/*
 * Copyright (C) 2019,2020 IBM Corporation
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
 */
package examples;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Connection;
import io.vertx.db2client.DB2Pool;
import io.vertx.docgen.Source;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;

@Source
public class DB2ClientExamples {

  public void gettingStarted() {

    // Connect options
    DB2ConnectOptions connectOptions = new DB2ConnectOptions()
      .setPort(50000)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the client pool
    DB2Pool client = DB2Pool.pool(connectOptions, poolOptions);

    // A simple query
    client
      .query("SELECT * FROM users WHERE id='julien'")
      .execute(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> result = ar.result();
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
    DB2ConnectOptions connectOptions = new DB2ConnectOptions()
      .setPort(50000)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool Options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pool from the data object
    DB2Pool pool = DB2Pool.pool(vertx, connectOptions, poolOptions);

    pool.getConnection(ar -> {
      // Handling your connection
    });
  }

  public void configureFromUri(Vertx vertx) {

    // Connection URI
    String connectionUri = "db2://dbuser:secretpassword@database.server.com:50000/mydb";

    // Create the pool from the connection URI
    DB2Pool pool = DB2Pool.pool(connectionUri);

    // Create the connection from the connection URI
    DB2Connection.connect(vertx, connectionUri, res -> {
      // Handling your connection
    });
  }

  public void connecting01() {

    // Connect options
    DB2ConnectOptions connectOptions = new DB2ConnectOptions()
      .setPort(50000)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    DB2Pool client = DB2Pool.pool(connectOptions, poolOptions);
  }

  public void connecting02(Vertx vertx) {

    // Connect options
    DB2ConnectOptions connectOptions = new DB2ConnectOptions()
      .setPort(50000)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    // Create the pooled client
    DB2Pool client = DB2Pool.pool(vertx, connectOptions, poolOptions);
  }

  public void connecting03(DB2Pool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Connect options
    DB2ConnectOptions connectOptions = new DB2ConnectOptions()
      .setPort(50000)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    DB2Pool client = DB2Pool.pool(vertx, connectOptions, poolOptions);

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

  public void connecting05(Vertx vertx) {

    // Pool options
    DB2ConnectOptions options = new DB2ConnectOptions()
      .setPort(50000)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Connect to DB2
    DB2Connection.connect(vertx, options)
      .compose(conn -> {
        System.out.println("Connected");

        // All operations execute on the same connection
        return conn
          .query("SELECT * FROM users WHERE id='julien'")
          .execute()
          .compose(res -> conn
            .query("SELECT * FROM users WHERE id='emad'")
            .execute()
          ).onComplete(ar -> {
            // Close the connection
            conn.close();
          });
      }).onComplete(res -> {
      if (res.succeeded()) {

        System.out.println("Done");
      } else {
        System.out.println("Could not connect: " + res.cause().getMessage());
      }
    });
  }
  
  public void generatedKeys(SqlClient client) {
    client
      .preparedQuery("SELECT color_id FROM FINAL TABLE ( INSERT INTO color (color_name) VALUES (?), (?), (?) )")
      .execute(Tuple.of("white", "red", "blue"), ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        System.out.println("Inserted " + rows.rowCount() + " new rows.");
        for (Row row : rows) {
          System.out.println("generated key: " + row.getInteger("color_id"));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }
  
  public void typeMapping01(Pool pool) {
    pool
      .query("SELECT an_int_column FROM exampleTable")
      .execute(ar -> {
      RowSet<Row> rowSet = ar.result();
      Row row = rowSet.iterator().next();

      // Stored as INTEGER column type and represented as java.lang.Integer
      Object value = row.getValue(0);

      // Convert to java.lang.Long
      Long longValue = row.getLong(0);
    });
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
    client.query("SELECT * FROM users").collecting(collector).execute(ar -> {
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

}

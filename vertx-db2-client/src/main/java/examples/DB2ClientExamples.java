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

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import io.vertx.core.Vertx;
import io.vertx.db2client.DB2ConnectOptions;
import io.vertx.db2client.DB2Connection;
import io.vertx.db2client.DB2Pool;
import io.vertx.docgen.Source;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.SqlResult;

@Source
public class DB2ClientExamples {

  public void gettingStarted() {

    // Connect options
    DB2ConnectOptions connectOptions = new DB2ConnectOptions()
      .setPort(5432)
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
      .setPort(5432)
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

  public void configureDefaultSchema() {
    // Data object
    DB2ConnectOptions connectOptions = new DB2ConnectOptions();

    // Set the default schema
    connectOptions.addProperty("search_path", "myschema");
  }

  public void configureFromUri(Vertx vertx) {

    // Connection URI
    String connectionUri = "postgresql://dbuser:secretpassword@database.server.com:3211/mydb";

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
      .setPort(5432)
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
      .setPort(5432)
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
      .setPort(5432)
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
    client.getConnection(ar1 -> {

      if (ar1.succeeded()) {

        System.out.println("Connected");

        // Obtain our connection
        SqlConnection conn = ar1.result();

        // All operations execute on the same connection
        conn
          .query("SELECT * FROM users WHERE id='julien'")
          .execute(ar2 -> {
          if (ar2.succeeded()) {
            conn
              .query("SELECT * FROM users WHERE id='emad'")
              .execute(ar3 -> {
              // Release the connection to the pool
              conn.close();
            });
          } else {
            // Release the connection to the pool
            conn.close();
          }
        });
      } else {
        System.out.println("Could not connect: " + ar1.cause().getMessage());
      }
    });
  }

  public void connecting05(Vertx vertx) {

    // Pool options
    DB2ConnectOptions options = new DB2ConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Connect to Postgres
    DB2Connection.connect(vertx, options, res -> {
      if (res.succeeded()) {

        System.out.println("Connected");

        // Obtain our connection
        DB2Connection conn = res.result();

        // All operations execute on the same connection
        conn
          .query("SELECT * FROM users WHERE id='julien'")
          .execute(ar2 -> {
          if (ar2.succeeded()) {
            conn
              .query("SELECT * FROM users WHERE id='emad'")
              .execute(ar3 -> {
              // Close the connection
              conn.close();
            });
          } else {
            // Close the connection
            conn.close();
          }
        });
      } else {
        System.out.println("Could not connect: " + res.cause().getMessage());
      }
    });
  }

  public void connecting06(Vertx vertx) {

    // Connect Options
    // Socket file name will be /var/run/postgresql/.s.PGSQL.5432
    DB2ConnectOptions connectOptions = new DB2ConnectOptions()
      .setHost("/var/run/postgresql")
      .setPort(5432)
      .setDatabase("the-db");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    DB2Pool client = DB2Pool.pool(connectOptions, poolOptions);

    // Create the pooled client with a vertx instance
    // Make sure the vertx instance has enabled native transports
    DB2Pool client2 = DB2Pool.pool(vertx, connectOptions, poolOptions);
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

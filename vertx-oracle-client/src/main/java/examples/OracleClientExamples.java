/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package examples;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;
import io.vertx.oracleclient.OracleClient;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.oracleclient.OraclePool;
import io.vertx.oracleclient.OraclePrepareOptions;
import io.vertx.oracleclient.data.Blob;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Source
@SuppressWarnings("unused")
public class OracleClientExamples {

  public void gettingStarted() {

    // Connect options
    OracleConnectOptions connectOptions = new OracleConnectOptions()
      .setPort(1521)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the client pool
    OraclePool client = OraclePool.pool(connectOptions, poolOptions);

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
    OracleConnectOptions connectOptions = new OracleConnectOptions()
      .setPort(1521)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool Options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pool from the data object
    OraclePool pool = OraclePool.pool(vertx, connectOptions, poolOptions);

    pool.getConnection(ar -> {
      // Handling your connection
    });
  }

  public void configureFromUri(Vertx vertx) {

    // Connection URI
    String connectionUri = "oracle:thin:scott/tiger@myhost:1521:orcl";

    // Pool Options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pool from the connection URI
    OraclePool pool = OraclePool.pool(connectionUri, poolOptions);
  }

  public void connecting01() {

    // Connect options
    OracleConnectOptions connectOptions = new OracleConnectOptions()
      .setPort(1521)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    OraclePool client = OraclePool.pool(connectOptions, poolOptions);
  }


  public void connecting02(Vertx vertx) {

    // Connect options
    OracleConnectOptions connectOptions = new OracleConnectOptions()
      .setPort(1521)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    // Create the pooled client
    OraclePool client = OraclePool.pool(vertx, connectOptions, poolOptions);
  }

  public void connecting03(Pool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Connect options
    OracleConnectOptions connectOptions = new OracleConnectOptions()
      .setPort(1521)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    OraclePool client = OraclePool.pool(vertx, connectOptions, poolOptions);

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

  public void reconnectAttempts(OracleConnectOptions options) {
    // The client will try to connect at most 3 times at a 1 second interval
    options
      .setReconnectAttempts(2)
      .setReconnectInterval(1000);
  }

  public void implicitTypeConversionExample(SqlClient client) {
    client
      .preparedQuery("SELECT * FROM students WHERE updated_time = ?")
      .execute(Tuple.of(LocalTime.of(19, 10, 25)), ar -> {
      // handle the results
    });
    // this will also work with implicit type conversion
    client
      .preparedQuery("SELECT * FROM students WHERE updated_time = ?")
      .execute(Tuple.of("19:10:25"), ar -> {
      // handle the results
    });
  }

  public void booleanExample01(SqlClient client) {
    client
      .query("SELECT graduated FROM students WHERE id = 0")
      .execute(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rowSet = ar.result();
        for (Row row : rowSet) {
          int pos = row.getColumnIndex("graduated");
          Byte value = row.get(Byte.class, pos);
          Boolean graduated = row.getBoolean("graduated");
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void booleanExample02(SqlClient client) {
    client
      .preparedQuery("UPDATE students SET graduated = ? WHERE id = 0")
      .execute(Tuple.of(true), ar -> {
      if (ar.succeeded()) {
        System.out.println("Updated with the boolean value");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void jsonExample() {

    // Create a tuple
    Tuple tuple = Tuple.of(
      Tuple.JSON_NULL,
      new JsonObject().put("foo", "bar"),
      3);

    // Retrieving json
    Object value = tuple.getValue(0); // Expect JSON_NULL

    //
    value = tuple.get(JsonObject.class, 1); // Expect JSON object

    //
    value = tuple.get(Integer.class, 2); // Expect 3
    value = tuple.getInteger(2); // Expect 3
  }

  public void numericExample(Row row) {
    Numeric numeric = row.get(Numeric.class, 0);
    if (numeric.isNaN()) {
      // Handle NaN
    } else {
      BigDecimal value = numeric.bigDecimalValue();
    }
  }

  enum Color {
    red
  }

  public void enumeratedType01Example(SqlClient client) {
    client
      .preparedQuery("INSERT INTO colors VALUES (?)")
      .execute(Tuple.of(Color.red), res -> {
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

  public void geometryExample01(SqlClient client) {
    client
      .query("SELECT ST_AsText(g) FROM geom;")
      .execute(ar -> {
      if (ar.succeeded()) {
        // Fetch the spatial data in WKT format
        RowSet<Row> result = ar.result();
        for (Row row : result) {
          String wktString = row.getString(0);
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void geometryExample02(SqlClient client) {
    client
      .query("SELECT ST_AsBinary(g) FROM geom;")
      .execute(ar -> {
      if (ar.succeeded()) {
        // Fetch the spatial data in WKB format
        RowSet<Row> result = ar.result();
        for (Row row : result) {
          Buffer wkbValue = row.getBuffer(0);
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void collector01Example(SqlClient client) {

    // Create a collector projecting a row set to a map
    Collector<Row, ?, Map<Long, String>> collector = Collectors.toMap(
      row -> row.getLong("id"),
      row -> row.getString("last_name"));

    // Run the query with the collector
    client.query("SELECT * FROM users").collecting(collector).execute(ar -> {
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

  public void storedProcedureExample(SqlClient client) {
    client.query("CREATE PROCEDURE multi() BEGIN\n" +
      "  SELECT 1;\n" +
      "  SELECT 1;\n" +
      "  INSERT INTO ins VALUES (1);\n" +
      "  INSERT INTO ins VALUES (2);\n" +
      "END;").execute(ar1 -> {
      if (ar1.succeeded()) {
        // create stored procedure success
        client
          .query("CALL multi();")
          .execute(ar2 -> {
          if (ar2.succeeded()) {
            // handle the result
            RowSet<Row> result1 = ar2.result();
            Row row1 = result1.iterator().next();
            System.out.println("First result: " + row1.getInteger(0));

            RowSet<Row> result2 = result1.next();
            Row row2 = result2.iterator().next();
            System.out.println("Second result: " + row2.getInteger(0));

            RowSet<Row> result3 = result2.next();
            System.out.println("Affected rows: " + result3.rowCount());
          } else {
            System.out.println("Failure: " + ar2.cause().getMessage());
          }
        });
      } else {
        System.out.println("Failure: " + ar1.cause().getMessage());
      }
    });
  }

  public void retrieveGeneratedKeyByName(SqlClient client) {
    String sql = "INSERT INTO EntityWithIdentity (name, position) VALUES (?, ?)";

    // Retrieve generated key column value by name
    OraclePrepareOptions options = new OraclePrepareOptions()
      .setAutoGeneratedKeysIndexes(new JsonArray().add("ID"));

    client.preparedQuery(sql, options).execute(Tuple.of("john", 3), ar -> {
      if (ar.succeeded()) {
        RowSet<Row> result = ar.result();

        Row generated = result.property(OracleClient.GENERATED_KEYS);
        Long id = generated.getLong("ID");
      }
    });
  }

  public void retrieveGeneratedKeyByIndex(SqlClient client) {
    String sql = "INSERT INTO EntityWithIdentity (name, position) VALUES (?, ?)";

    // Retrieve generated key column value by index
    OraclePrepareOptions options = new OraclePrepareOptions()
      .setAutoGeneratedKeysIndexes(new JsonArray().add("1"));

    client.preparedQuery(sql, options).execute(Tuple.of("john", 3), ar -> {
      if (ar.succeeded()) {
        RowSet<Row> result = ar.result();

        Row generated = result.property(OracleClient.GENERATED_KEYS);
        Long id = generated.getLong("ID");
      }
    });
  }

  public void blobUsage(SqlClient client, Buffer imageBuffer, Long id) {
    client.preparedQuery("INSERT INTO images (name, data) VALUES (?, ?)")
      // Use io.vertx.oracleclient.data.Blob when inserting
      .execute(Tuple.of("beautiful-sunset.jpg", Blob.copy(imageBuffer)))
      .onComplete(ar -> {
        // Do something
      });

    client.preparedQuery("SELECT data FROM images WHERE id = ?")
      .execute(Tuple.of(id))
      .onComplete(ar -> {
        if (ar.succeeded()) {
          Row row = ar.result().iterator().next();

          // Use io.vertx.core.buffer.Buffer when reading
          Buffer data = row.getBuffer("data");
        }
      });
  }
}

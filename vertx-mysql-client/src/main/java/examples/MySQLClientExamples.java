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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.docgen.Source;
import io.vertx.mysqlclient.*;
import io.vertx.mysqlclient.data.spatial.Point;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Source
public class MySQLClientExamples {
  public void gettingStarted() {

    // Connect options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the client pool
    SqlClient client = MySQLPool.client(connectOptions, poolOptions);

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
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool Options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pool from the data object
    MySQLPool pool = MySQLPool.pool(vertx, connectOptions, poolOptions);

    pool.getConnection(ar -> {
      // Handling your connection
    });
  }

  public void configureConnectionCharset() {
    MySQLConnectOptions connectOptions = new MySQLConnectOptions();

    // set connection character set to utf8 instead of the default charset utf8mb4
    connectOptions.setCharset("utf8");
  }

  public void configureConnectionCollation() {
    MySQLConnectOptions connectOptions = new MySQLConnectOptions();

    // set connection collation to utf8_general_ci instead of the default collation utf8mb4_general_ci
    // setting a collation will override the charset option
    connectOptions.setCharset("gbk");
    connectOptions.setCollation("utf8_general_ci");
  }

  public void configureConnectionAttributes() {
    // Data object
    MySQLConnectOptions connectOptions = new MySQLConnectOptions();

    // Add a connection attribute
    connectOptions.addProperty("_java_version", "1.8.0_212");

    // Override the attributes
    Map<String, String> attributes = new HashMap<>();
    attributes.put("_client_name", "myapp");
    attributes.put("_client_version", "1.0.0");
    connectOptions.setProperties(attributes);
  }

  public void configureFromUri(Vertx vertx) {

    // Connection URI
    String connectionUri = "mysql://dbuser:secretpassword@database.server.com:3306/mydb";

    // Create the pool from the connection URI
    MySQLPool pool = MySQLPool.pool(connectionUri);

    // Create the connection from the connection URI
    MySQLConnection.connect(vertx, connectionUri, res -> {
      // Handling your connection
    });
  }

  public void connecting01() {

    // Connect options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MySQLPool client = MySQLPool.pool(connectOptions, poolOptions);
  }


  public void connecting02(Vertx vertx) {

    // Connect options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    // Create the pooled client
    MySQLPool client = MySQLPool.pool(vertx, connectOptions, poolOptions);
  }

  public void connecting03(Pool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Connect options
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MySQLPool pool = MySQLPool.pool(vertx, connectOptions, poolOptions);

    // Get a connection from the pool
    pool.getConnection().compose(conn -> {
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

  public void clientPipelining(Vertx vertx, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {
    MySQLPool pool = MySQLPool.pool(vertx, connectOptions.setPipeliningLimit(16), poolOptions);
  }

  public void poolVersusPooledClient(Vertx vertx, String sql, MySQLConnectOptions connectOptions, PoolOptions poolOptions) {

    // Pooled client
    connectOptions.setPipeliningLimit(64);
    SqlClient client = MySQLPool.client(vertx, connectOptions, poolOptions);

    // Pipelined
    Future<RowSet<Row>> res1 = client.query(sql).execute();

    // Connection pool
    MySQLPool pool = MySQLPool.pool(vertx, connectOptions, poolOptions);

    // Not pipelined
    Future<RowSet<Row>> res2 = pool.query(sql).execute();
  }

  public void connectWithUnixDomainSocket(Vertx vertx) {
    // Connect Options
    // Socket file name /var/run/mysqld/mysqld.sock
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setHost("/var/run/mysqld/mysqld.sock")
      .setDatabase("the-db");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    MySQLPool client = MySQLPool.pool(connectOptions, poolOptions);

    // Create the pooled client with a vertx instance
    // Make sure the vertx instance has enabled native transports
    // vertxOptions.setPreferNativeTransport(true);
    MySQLPool client2 = MySQLPool.pool(vertx, connectOptions, poolOptions);
  }

  public void reconnectAttempts(MySQLConnectOptions options) {
    // The client will try to connect at most 3 times at a 1 second interval
    options
      .setReconnectAttempts(2)
      .setReconnectInterval(1000);
  }

  public void lastInsertId(SqlClient client) {
    client
      .query("INSERT INTO test(val) VALUES ('v1')")
      .execute(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
          System.out.println("Last inserted id is: " + lastInsertId);
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
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

  public void geometryExample03(SqlClient client) {
    client
      .query("SELECT g FROM geom;")
      .execute(ar -> {
      if (ar.succeeded()) {
        // Fetch the spatial data as a Vert.x Data Object
        RowSet<Row> result = ar.result();
        for (Row row : result) {
          Point point = row.get(Point.class, 0);
          System.out.println("Point x: " + point.getX());
          System.out.println("Point y: " + point.getY());
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void geometryExample04(SqlClient client) {
    Point point = new Point(0, 1.5, 1.5);
    // Send as a WKB representation
    client
      .preparedQuery("INSERT INTO geom VALUES (ST_GeomFromWKB(?))")
      .execute(Tuple.of(point), ar -> {
      if (ar.succeeded()) {
        System.out.println("Success");
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

  public void defaultAuthPluginExample() {
    MySQLConnectOptions options = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret")
      .setAuthenticationPlugin(MySQLAuthenticationPlugin.MYSQL_NATIVE_PASSWORD); // set the default authentication plugin
  }

  public void rsaPublicKeyExample() {

    MySQLConnectOptions options1 = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret")
      .setServerRsaPublicKeyPath("tls/files/public_key.pem"); // configure with path of the public key

    MySQLConnectOptions options2 = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret")
      .setServerRsaPublicKeyValue(Buffer.buffer("-----BEGIN PUBLIC KEY-----\n" +
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3yvG5s0qrV7jxVlp0sMj\n" +
        "xP0a6BuLKCMjb0o88hDsJ3xz7PpHNKazuEAfPxiRFVAV3edqfSiXoQw+lJf4haEG\n" +
        "HQe12Nfhs+UhcAeTKXRlZP/JNmI+BGoBduQ1rCId9bKYbXn4pvyS/a1ft7SwFkhx\n" +
        "aogCur7iIB0WUWvwkQ0fEj/Mlhw93lLVyx7hcGFq4FOAKFYr3A0xrHP1IdgnD8QZ\n" +
        "0fUbgGLWWLOossKrbUP5HWko1ghLPIbfmU6o890oj1ZWQewj1Rs9Er92/UDj/JXx\n" +
        "7ha1P+ZOgPBlV037KDQMS6cUh9vTablEHsMLhDZanymXzzjBkL+wH/b9cdL16LkQ\n" +
        "5QIDAQAB\n" +
        "-----END PUBLIC KEY-----\n")); // configure with buffer of the public key
  }

  public void tlsExample(Vertx vertx) {

    MySQLConnectOptions options = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret")
      .setSslMode(SslMode.VERIFY_CA)
      .setPemTrustOptions(new PemTrustOptions().addCertPath("/path/to/cert.pem"));

    MySQLConnection.connect(vertx, options, res -> {
      if (res.succeeded()) {
        // Connected with SSL
      } else {
        System.out.println("Could not connect " + res.cause());
      }
    });
  }

  public void pingExample(MySQLConnection connection) {
    connection.ping(ar -> {
      System.out.println("The server has responded to the PING");
    });
  }

  public void resetConnectionExample(MySQLConnection connection) {
    connection.resetConnection(ar -> {
      if (ar.succeeded()) {
        System.out.println("Connection has been reset now");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void changeUserExample(MySQLConnection connection) {
    MySQLAuthOptions authenticationOptions = new MySQLAuthOptions()
      .setUser("newuser")
      .setPassword("newpassword")
      .setDatabase("newdatabase");
    connection.changeUser(authenticationOptions, ar -> {
      if (ar.succeeded()) {
        System.out.println("User of current connection has been changed.");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void initDbExample(MySQLConnection connection) {
    connection.specifySchema("newschema", ar -> {
      if (ar.succeeded()) {
        System.out.println("Default schema changed to newschema");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void statisticsExample(MySQLConnection connection) {
    connection.getInternalStatistics(ar -> {
      if (ar.succeeded()) {
        System.out.println("Statistics: " + ar.result());
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void debugExample(MySQLConnection connection) {
    connection.debug(ar -> {
      if (ar.succeeded()) {
        System.out.println("Debug info dumped to server's STDOUT");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void setOptionExample01(MySQLConnection connection) {
    connection.setOption(MySQLSetOption.MYSQL_OPTION_MULTI_STATEMENTS_OFF, ar -> {
      if (ar.succeeded()) {
        System.out.println("CLIENT_MULTI_STATEMENTS is off now");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }
}

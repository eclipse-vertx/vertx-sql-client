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
package examples;

import io.vertx.core.Vertx;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.docgen.Source;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Source
public class SqlClientExamples {

  public void queries01(SqlClient client) {
    client
      .query("SELECT * FROM users WHERE id='julien'")
      .execute(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> result = ar.result();
        System.out.println("Got " + result.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }


  public void queries02(SqlClient client) {
    client
      .preparedQuery("SELECT * FROM users WHERE id=?")
      .execute(Tuple.of("julien"), ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        System.out.println("Got " + rows.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries03(SqlClient client) {
    client
      .preparedQuery("SELECT first_name, last_name FROM users")
      .execute(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        for (Row row : rows) {
          System.out.println("User " + row.getString(0) + " " + row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries04(SqlClient client) {
    client
      .preparedQuery("INSERT INTO users (first_name, last_name) VALUES (?, ?)")
      .execute(Tuple.of("Julien", "Viet"), ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        System.out.println(rows.rowCount());
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries05(Row row) {
    System.out.println("User " + row.getString(0) + " " + row.getString(1));
  }

  public void queries06(Row row) {
    System.out.println("User " + row.getString("first_name") + " " + row.getString("last_name"));
  }

  public void queries07(Row row) {

    String firstName = row.getString("first_name");
    Boolean male = row.getBoolean("male");
    Integer age = row.getInteger("age");

    // ...

  }

  public void queries08(SqlClient client) {

    // Add commands to the batch
    List<Tuple> batch = new ArrayList<>();
    batch.add(Tuple.of("julien", "Julien Viet"));
    batch.add(Tuple.of("emad", "Emad Alblueshi"));

    // Execute the prepared batch
    client
      .preparedQuery("INSERT INTO USERS (id, name) VALUES (?, ?)")
      .executeBatch(batch, res -> {
      if (res.succeeded()) {

        // Process rows
        RowSet<Row> rows = res.result();
      } else {
        System.out.println("Batch failed " + res.cause());
      }
    });
  }

  public void queries09(SqlClient client, SqlConnectOptions connectOptions) {

    // Enable prepare statements caching
    connectOptions.setCachePreparedStatements(true);
    client
      .preparedQuery("SELECT * FROM users WHERE id = ?")
      .execute(Tuple.of("julien"), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          System.out.println("Got " + rows.size() + " rows ");
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  public void queries10(SqlConnection sqlConnection) {
    sqlConnection
      .prepare("SELECT * FROM users WHERE id = ?", ar -> {
        if (ar.succeeded()) {
          PreparedStatement preparedStatement = ar.result();
          preparedStatement.query()
            .execute(Tuple.of("julien"), ar2 -> {
              if (ar2.succeeded()) {
                RowSet<Row> rows = ar2.result();
                System.out.println("Got " + rows.size() + " rows ");
                preparedStatement.close();
              } else {
                System.out.println("Failure: " + ar2.cause().getMessage());
              }
            });
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  public void usingConnections01(Vertx vertx, Pool pool) {

    pool
      .getConnection()
      .compose(connection ->
        connection
          .preparedQuery("INSERT INTO Users (first_name,last_name) VALUES (?, ?)")
          .executeBatch(Arrays.asList(
            Tuple.of("Julien", "Viet"),
            Tuple.of("Emad", "Alblueshi")
          ))
          .compose(res -> connection
            // Do something with rows
            .query("SELECT COUNT(*) FROM Users")
            .execute()
            .map(rows -> rows.iterator().next().getInteger(0)))
          // Return the connection to the pool
          .eventually(v -> connection.close())
      ).onSuccess(count -> {
      System.out.println("Insert users, now the number of users is " + count);
    });
  }

  public void usingConnections02(SqlConnection connection) {
    connection
      .prepare("SELECT * FROM users WHERE first_name LIKE ?")
      .compose(pq ->
        pq.query()
          .execute(Tuple.of("Julien"))
          .eventually(v -> pq.close())
      ).onSuccess(rows -> {
      // All rows
    });
  }

  public void usingConnections03(Pool pool) {
    pool.withConnection(connection ->
      connection
        .preparedQuery("INSERT INTO Users (first_name,last_name) VALUES (?, ?)")
        .executeBatch(Arrays.asList(
          Tuple.of("Julien", "Viet"),
          Tuple.of("Emad", "Alblueshi")
        ))
        .compose(res -> connection
          // Do something with rows
          .query("SELECT COUNT(*) FROM Users")
          .execute()
          .map(rows -> rows.iterator().next().getInteger(0)))
    ).onSuccess(count -> {
      System.out.println("Insert users, now the number of users is " + count);
    });
  }

  public void transaction01(Pool pool) {
    pool.getConnection()
      // Transaction must use a connection
      .onSuccess(conn -> {
        // Begin the transaction
        conn.begin()
          .compose(tx -> conn
            // Various statements
            .query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
            .execute()
            .compose(res2 -> conn
              .query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')")
              .execute())
            // Commit the transaction
            .compose(res3 -> tx.commit()))
          // Return the connection to the pool
          .eventually(v -> conn.close())
          .onSuccess(v -> System.out.println("Transaction succeeded"))
          .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
      });
  }

  public void transaction02(Transaction tx) {
    tx.completion()
      .onFailure(err -> {
        System.out.println("Transaction failed => rolled back");
      });
  }

  public void transaction03(Pool pool) {
    // Acquire a transaction and begin the transaction
    pool.withTransaction(client -> client
      .query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
      .execute()
      .flatMap(res -> client
        .query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')")
        .execute()
        // Map to a message result
        .map("Users inserted")))
      .onSuccess(v -> System.out.println("Transaction succeeded"))
      .onFailure(err -> System.out.println("Transaction failed: " + err.getMessage()));
  }

  public void usingCursors01(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE age > ?", ar1 -> {
      if (ar1.succeeded()) {
        PreparedStatement pq = ar1.result();

        // Create a cursor
        Cursor cursor = pq.cursor(Tuple.of(18));

        // Read 50 rows
        cursor.read(50, ar2 -> {
          if (ar2.succeeded()) {
            RowSet<Row> rows = ar2.result();

            // Check for more ?
            if (cursor.hasMore()) {
              // Repeat the process...
            } else {
              // No more rows - close the cursor
              cursor.close();
            }
          }
        });
      }
    });
  }

  public void usingCursors02(Cursor cursor) {
    cursor.read(50, ar2 -> {
      if (ar2.succeeded()) {
        // Close the cursor
        cursor.close();
      }
    });
  }

  public void usingCursors03(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE age > ?", ar1 -> {
      if (ar1.succeeded()) {
        PreparedStatement pq = ar1.result();

        // Fetch 50 rows at a time
        RowStream<Row> stream = pq.createStream(50, Tuple.of(18));

        // Use the stream
        stream.exceptionHandler(err -> {
          System.out.println("Error: " + err.getMessage());
        });
        stream.endHandler(v -> {
          System.out.println("End of stream");
        });
        stream.handler(row -> {
          System.out.println("User: " + row.getString("last_name"));
        });
      }
    });
  }

  public void tracing01(OracleConnectOptions options) {
    options.setTracingPolicy(TracingPolicy.ALWAYS);
  }

}

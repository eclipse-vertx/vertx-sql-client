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
import io.vertx.docgen.Source;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
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
      .preparedQuery("SELECT * FROM users WHERE id=$1")
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
      .preparedQuery("INSERT INTO users (first_name, last_name) VALUES ($1, $2)")
      .execute(Tuple.of("Julien", "Viet"),  ar -> {
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
      .preparedQuery("INSERT INTO USERS (id, name) VALUES ($1, $2)")
      .executeBatch(batch, res -> {
      if (res.succeeded()) {

        // Process rows
        RowSet<Row> rows = res.result();
      } else {
        System.out.println("Batch failed " + res.cause());
      }
    });
  }

  public void queries09(Vertx vertx, SqlConnectOptions connectOptions, PoolOptions poolOptions) {

    // Enable prepare statements caching
    connectOptions.setCachePreparedStatements(true);
  }

  public void usingConnections01(Vertx vertx, Pool pool) {

    pool.getConnection(ar1 -> {
      if (ar1.succeeded()) {
        SqlConnection connection = ar1.result();

        connection
          .query("SELECT * FROM users WHERE id='julien'")
          .execute(ar2 -> {
          if (ar1.succeeded()) {
            connection
              .query("SELECT * FROM users WHERE id='paulo'")
              .execute(ar3 -> {
              // Do something with rows and return the connection to the pool
              connection.close();
            });
          } else {
            // Return the connection to the pool
            connection.close();
          }
        });
      }
    });
  }

  public void usingConnections02(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PreparedStatement pq = ar1.result();
        pq.query().execute(Tuple.of("julien"), ar2 -> {
          if (ar2.succeeded()) {
            // All rows
            RowSet<Row> rows = ar2.result();
          }
        });
      }
    });
  }

  public void usingConnections03(SqlConnection connection) {
    connection.prepare("INSERT INTO USERS (id, name) VALUES ($1, $2)", ar1 -> {
      if (ar1.succeeded()) {
        PreparedStatement prepared = ar1.result();

        // Create a query : bind parameters
        List<Tuple> batch = new ArrayList();

        // Add commands to the createBatch
        batch.add(Tuple.of("julien", "Julien Viet"));
        batch.add(Tuple.of("emad", "Emad Alblueshi"));

        prepared.query().executeBatch(batch, res -> {
          if (res.succeeded()) {

            // Process rows
            RowSet<Row> rows = res.result();
          } else {
            System.out.println("Batch failed " + res.cause());
          }
        });
      }
    });
  }

  public void transaction01(Pool pool) {
    pool.getConnection(res -> {
      if (res.succeeded()) {

        // Transaction must use a connection
        SqlConnection conn = res.result();

        // Begin the transaction
        Transaction tx = conn.begin();

        // Various statements
        conn
          .query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
          .execute(ar1 -> {
          if (ar1.succeeded()) {
            conn
              .query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')")
              .execute(ar2 -> {
              if (ar2.succeeded()) {
                // Commit the transaction
                tx.commit(ar3 -> {
                  if (ar3.succeeded()) {
                    System.out.println("Transaction succeeded");
                  } else {
                    System.out.println("Transaction failed " + ar3.cause().getMessage());
                  }
                  // Return the connection to the pool
                  conn.close();
                });
              } else {
                // Return the connection to the pool
                conn.close();
              }
            });
          } else {
            // Return the connection to the pool
            conn.close();
          }
        });
      }
    });
  }

  public void transaction02(Transaction tx) {
    tx.abortHandler(v -> {
      System.out.println("Transaction failed => rollbacked");
    });
  }

  public void transaction03(Pool pool) {

    // Acquire a transaction and begin the transaction
    pool.begin(res -> {
      if (res.succeeded()) {

        // Get the transaction
        Transaction tx = res.result();

        // Various statements
        tx.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
          .execute(ar1 -> {
          if (ar1.succeeded()) {
            tx.query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')")
              .execute(ar2 -> {
              if (ar2.succeeded()) {
                // Commit the transaction
                // the connection will automatically return to the pool
                tx.commit(ar3 -> {
                  if (ar3.succeeded()) {
                    System.out.println("Transaction succeeded");
                  } else {
                    System.out.println("Transaction failed " + ar3.cause().getMessage());
                  }
                });
              }
            });
          } else {
            // No need to close connection as transaction will abort and be returned to the pool
          }
        });
      }
    });
  }

  public void usingCursors01(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PreparedStatement pq = ar1.result();

        // Cursors require to run within a transaction
        Transaction tx = connection.begin();

        // Create a cursor
        Cursor cursor = pq.cursor(Tuple.of("julien"));

        // Read 50 rows
        cursor.read(50, ar2 -> {
          if (ar2.succeeded()) {
            RowSet<Row> rows = ar2.result();

            // Check for more ?
            if (cursor.hasMore()) {
              // Repeat the process...
            } else {
              // No more rows - commit the transaction
              tx.commit();
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
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PreparedStatement pq = ar1.result();

        // Streams require to run within a transaction
        Transaction tx = connection.begin();

        // Fetch 50 rows at a time
        RowStream<Row> stream = pq.createStream(50, Tuple.of("julien"));

        // Use the stream
        stream.exceptionHandler(err -> {
          System.out.println("Error: " + err.getMessage());
        });
        stream.endHandler(v -> {
          tx.commit();
          System.out.println("End of stream");
        });
        stream.handler(row -> {
          System.out.println("User: " + row.getString("last_name"));
        });
      }
    });
  }
}

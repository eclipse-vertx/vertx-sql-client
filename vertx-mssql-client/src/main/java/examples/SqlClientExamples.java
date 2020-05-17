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
import io.vertx.sqlclient.*;

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
      .preparedQuery("SELECT * FROM users WHERE id=@p1")
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
      .preparedQuery("INSERT INTO users (first_name, last_name) VALUES (@p1, @p2)")
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
      .preparedQuery("INSERT INTO USERS (id, name) VALUES (@p1, @p2)")
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
      .preparedQuery("SELECT * FROM users WHERE id = @p1")
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
      .prepare("SELECT * FROM users WHERE id = @p1", ar -> {
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
    connection.prepare("SELECT * FROM users WHERE first_name LIKE @p1", ar1 -> {
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
    connection.prepare("INSERT INTO USERS (id, name) VALUES (@p1, @p2)", ar1 -> {
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
        conn.begin(ar0 -> {
          if (ar0.succeeded()) {
            Transaction tx = ar0.result();

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
    });
  }

  public void transaction02(Transaction tx) {
    tx.completion().onFailure(err -> {
      System.out.println("Transaction failed => rollbacked");
    });
  }

  public void transaction03(Pool pool) {

    // Acquire a transaction and begin the transaction
    pool.withTransaction(client -> client
      .query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
      .execute()
      .flatMap(res -> client
        .query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
        .execute()
        // Map to a message result
        .map("Users inserted"))
    ).onComplete(ar -> {
      // The connection was automatically return to the pool
      if (ar.succeeded()) {
        // Transaction was committed
        String message = ar.result();
        System.out.println("Transaction succeeded: " + message);
      } else {
        // Transaction was rolled back
        System.out.println("Transaction failed " + ar.cause().getMessage());
      }
    });
  }

  public void transaction04(SqlConnection sqlConnection) {

    sqlConnection.begin("START TRANSACTION READ ONLY", ar -> {
      if (ar.succeeded()) {
        // start a transaction which is read-only
        Transaction tx = ar.result();
      } else {
        // Failed to start a transaction
        System.out.println("Transaction failed " + ar.cause().getMessage());
      }
    });
  }

  public void transaction05(Pool pool) {
    pool.withTransaction("START TRANSACTION READ ONLY", client -> client
      .query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
      .execute()
    ).onFailure(error -> {
      // Failed to insert the record because the transaction is read-only
      System.out.println("Transaction failed " + error.getMessage());
    });
  }

  public void usingCursors01(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE age > @p1", ar1 -> {
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
    connection.prepare("SELECT * FROM users WHERE age > @p1", ar1 -> {
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
}

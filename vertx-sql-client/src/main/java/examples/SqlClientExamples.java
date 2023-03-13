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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.tracing.TracingPolicy;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

  public void queries09(SqlClient client, SqlConnectOptions connectOptions) {

    // Enable prepare statements caching
    connectOptions.setCachePreparedStatements(true);
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

  public void queries10(SqlConnection sqlConnection) {
    sqlConnection
      .prepare("SELECT * FROM users WHERE id=$1", ar -> {
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

  public void usingConnections03(Pool pool) {
    Future<Integer> future = pool.withConnection(conn -> conn
      .query("SELECT id FROM USERS WHERE name = 'Julien'")
      .execute()
      .flatMap(rowSet -> {
        Iterator<Row> rows = rowSet.iterator();
        if (rows.hasNext()) {
          Row row = rows.next();
          return Future.succeededFuture(row.getInteger("id"));
        } else {
          return Future.failedFuture("No results");
        }
      }));
    future.onSuccess(id -> {
      System.out.println("User id: " + id);
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
          } else {
            // Return the connection to the pool
            conn.close();
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

  public void usingCursors01(SqlConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar0 -> {
      if (ar0.succeeded()) {
        PreparedStatement pq = ar0.result();

        // Cursors require to run within a transaction
        connection.begin(ar1 -> {
          if (ar1.succeeded()) {
            Transaction tx = ar1.result();

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
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar0 -> {
      if (ar0.succeeded()) {
        PreparedStatement pq = ar0.result();

        // Streams require to run within a transaction
        connection.begin(ar1 -> {
          if (ar1.succeeded()) {
            Transaction tx = ar1.result();

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
    });
  }

  public void tracing01(SqlConnectOptions options) {
    options.setTracingPolicy(TracingPolicy.ALWAYS);
  }

  public void poolConfig01(SqlConnectOptions server1, SqlConnectOptions server2, SqlConnectOptions server3, PoolOptions options) {
    // Not generic
  }

  public void poolConfig02(Pool pool, String sql) {
    pool.connectHandler(conn -> {
      conn.query(sql).execute().onSuccess(res -> {
        // Release the connection to the pool, ready to be used by the application
        conn.close();
      });
    });
  }

  public void poolSharing1(Vertx vertx, SqlConnectOptions database, int maxSize) {
    Pool pool = Pool.pool(database, new PoolOptions().setMaxSize(maxSize));
    vertx.deployVerticle(() -> new AbstractVerticle() {
      @Override
      public void start() throws Exception {
        // Use the pool
      }
    }, new DeploymentOptions().setInstances(4));
  }

  public void poolSharing2(Vertx vertx, SqlConnectOptions database, int maxSize) {
    vertx.deployVerticle(() -> new AbstractVerticle() {
      Pool pool;
      @Override
      public void start() {
        // Get or create a shared pool
        // this actually creates a lease to the pool
        // when the verticle is undeployed, the lease will be released automaticaly
        pool = Pool.pool(database, new PoolOptions()
          .setMaxSize(maxSize)
          .setShared(true)
          .setName("my-pool"));
      }
    }, new DeploymentOptions().setInstances(4));
  }

  public static void poolSharing3(Vertx vertx, SqlConnectOptions database, int maxSize) {
    Pool pool = Pool.pool(database, new PoolOptions()
      .setMaxSize(maxSize)
      .setShared(true)
      .setName("my-pool")
      .setEventLoopSize(4));
  }
}

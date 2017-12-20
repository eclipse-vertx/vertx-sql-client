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

import com.julienviet.pgclient.*;
import com.julienviet.pgclient.pubsub.PgSubscriber;
import io.vertx.core.Vertx;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.docgen.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Source
public class Examples {

  public void gettingStarted() {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pool
    PgPool pool = PgClient.pool(options);

    // A simple query
    pool.query("SELECT * FROM users WHERE id='julien'", ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        System.out.println("Got " + result.size() + " results ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }

      // Close now the pool
      pool.close();
    });
  }

  public void connecting01() {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pool
    PgPool pool = PgClient.pool(options);
  }

  public void connecting02(Vertx vertx) {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pool
    PgPool pool = PgClient.pool(vertx, options);
  }

  public void connecting03(PgPool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Pool options
    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret");

    // Close the pool and all the associated resources
    PgClient.connect(vertx, options, res -> {
      if (res.succeeded()) {

        System.out.println("Connected");

        // Obtain our connection
        PgConnection conn = res.result();
      } else {
        System.out.println("Could not connect: " + res.cause().getMessage());
      }
    });
  }

  public void queries01(PgClient pool) {
    pool.query("SELECT * FROM users WHERE id='julien'", ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        System.out.println("Got " + result.size() + " results ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries02(PgClient pool) {
    pool.preparedQuery("SELECT * FROM users WHERE id=$1", Tuple.of("julien"),  ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        System.out.println("Got " + result.size() + " results ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries03(PgClient pool) {
    pool.preparedQuery("SELECT first_name, last_name FROM users", ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        for (Row row : result) {
          System.out.println("User " + row.getString(0) + " " + row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries04(PgClient pool) {
    pool.preparedQuery("\"INSERT INTO users (first_name, last_name) VALUES ($1, $2)", Tuple.of("Julien", "Viet"),  ar -> {
      if (ar.succeeded()) {
        PgResult<Row> result = ar.result();
        System.out.println(result.updatedCount());
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

  public void queries08(PgClient connection) {

    // Add commands to the batch
    List<Tuple> batch = new ArrayList<>();
    batch.add(Tuple.of("julien", "Julien Viet"));
    batch.add(Tuple.of("emad", "Emad Alblueshi"));

    // Execute the prepared batch
    connection.preparedBatch("INSERT INTO USERS (id, name) VALUES ($1, $2)", batch, res -> {
      if (res.succeeded()) {

        // Process results
        PgBatchResult<Row> results = res.result();
      } else {
        System.out.println("Batch failed " + res.cause());
      }
    });
  }

  public void queries09(Vertx vertx, PgPoolOptions options) {

    // Enable prepare statements
    options.setCachePreparedStatements(true);

    PgPool pool = PgClient.pool(vertx, options);
  }

  public void usingConnections01(Vertx vertx, PgPool pool) {

    pool.getConnection(ar1 -> {
      if (ar1.succeeded()) {
        PgConnection connection = ar1.result();

        connection.query("SELECT * FROM users WHERE id='julien'", ar2 -> {
          if (ar1.succeeded()) {
            connection.query("SELECT * FROM users WHERE id='paulo'", ar3 -> {
              // Do something with results and return the connection to the pool
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

  public void usingConnections02(PgConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedStatement pq = ar1.result();
        PgQuery query = pq.createQuery(Tuple.of("julien"));
        query.execute(ar2 -> {
          if (ar2.succeeded()) {
            // All rows
            PgResult<Row> result = ar2.result();
          }
        });
      }
    });
  }

  public void usingConnections03(PgConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedStatement pq = ar1.result();
        PgQuery query = pq.createQuery(Tuple.of("julien")).fetch(50);
        query.execute(ar2 -> {
          if (ar2.succeeded()) {
            PgResult<Row> result = ar2.result();

            // Check for more ?
            if (query.hasMore()) {
              query.execute(ar3 -> {
                // More results, and so on...
              });
            } else {
              // No more results
            }
          }
        });
      }
    });
  }

  public void usingConnections04(PgConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedStatement pq = ar1.result();
        PgQuery query = pq.createQuery(Tuple.of("julien")).fetch(50);
        query.execute(ar2 -> {
          if (ar2.succeeded()) {
            // Close the cursor
            query.close();
          }
        });
      }
    });
  }

  public void usingConnections05(PgConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedStatement pq = ar1.result();

        // Fetch 50 rows at a time
        PgStream<Row> stream = pq.createStream(50, Tuple.of("julien"));

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

  public void usingConnections06(PgConnection connection) {
    connection.prepare("INSERT INTO USERS (id, name) VALUES ($1, $2)", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgBatch batch = preparedStatement.createBatch();

        // Add commands to the createBatch
        batch.add(Tuple.of("julien", "Julien Viet"));
        batch.add(Tuple.of("emad", "Emad Alblueshi"));

        batch.execute(res -> {
          if (res.succeeded()) {

            // Process results
            PgBatchResult<Row> results = res.result();
          } else {
            System.out.println("Batch failed " + res.cause());
          }
        });
      }
    });
  }

  public void transaction01(PgPool pool) {
    pool.getConnection(res -> {
      if (res.succeeded()) {

        // Transaction must use a connection
        PgConnection conn = res.result();

        // Begin the transaction
        PgTransaction tx = conn.begin();

        // Various statements
        conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", ar -> {});
        conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')", ar -> {});

        // Commit the transaction
        tx.commit(ar -> {
          if (ar.succeeded()) {
            System.out.println("Transaction succeeded");
          } else {
            System.out.println("Transaction failed " + ar.cause().getMessage());
          }
        });
      }
    });
  }

  public void transaction02(PgPool pool) {
    pool.getConnection(res -> {
      if (res.succeeded()) {

        // Transaction must use a connection
        PgConnection conn = res.result();

        // Begin the transaction
        PgTransaction tx = conn
          .begin()
          .abortHandler(v -> {
          System.out.println("Transaction failed => rollbacked");
        });

        conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", ar -> {
          // Works fine of course
        });
        conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", ar -> {
          // Fails and triggers transaction aborts
        });

        // Attempt to commit the transaction
        tx.commit(ar -> {
          // But transaction abortion fails it
        });
      }
    });
  }

  public void pubsub01(PgConnection connection) {

    connection.notificationHandler(notification -> {
      System.out.println("Received " + notification.getPayload() + " on channel " + notification.getChannel());
    });

    connection.query("LISTEN some-channel", ar -> {
      System.out.println("Subscribed to channel");
    });
  }

  public void pubsub02(Vertx vertx) {

    PgSubscriber subscriber = PgSubscriber.subscriber(vertx, new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
    );

    // You can set the channel before connect
    subscriber.channel("channel1").handler(payload -> {
      System.out.println("Received " + payload);
    });

    subscriber.connect(ar -> {
      if (ar.succeeded()) {

        // Or you can set the channel after connect
        subscriber.channel("channel2").handler(payload -> {
          System.out.println("Received " + payload);
        });
      }
    });
  }

  public void pubsub03(Vertx vertx) {

    PgSubscriber subscriber = PgSubscriber.subscriber(vertx, new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
    );

    // Reconnect at most 10 times after 100 ms each
    subscriber.reconnectPolicy(retries -> {
      if (retries < 10) {
        return 100L;
      } else {
        return -1L;
      }
    });
  }

  public void ex1(Vertx vertx) {

    // Create options
    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret");

    // Connect
    PgClient.connect(vertx, options, res -> {
      if (res.succeeded()) {

        // Connected
        PgConnection conn = res.result();

        conn.createQuery("SELECT * FROM USERS").execute(ar -> {

          if (ar.succeeded()) {

            // Use result
            PgResult<Row> result = ar.result();
          } else {
            System.out.println("It failed");
          }

          // Close the connection
          conn.close();
        });
      } else {
        System.out.println("Could not connect " + res.cause());
      }
    });
  }

  public void ex2(Vertx vertx) {

    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setMaxSize(20);

    // Create a pool with 20 connections max
    PgPool pool = PgClient.pool(vertx, options);

    pool.getConnection(res -> {
      if (res.succeeded()) {

        // Obtained a connection
        PgConnection conn = res.result();

        conn.createQuery("SELECT * FROM USERS").execute(ar -> {

          if (ar.succeeded()) {

            // Use result set
            PgResult<Row> result = ar.result();
          } else {
            System.out.println("It failed");
          }

          // Return the connection to the pool
          conn.close();
        });
      } else {
        System.out.println("Could not obtain a connection " + res.cause());
      }
    });
  }

  public void ex3(PgPool pool) {

    // Close the pool and the connection it maintains
    pool.close();
  }

  public void ex4_(PgPool conn) {

    // Prepare (when not cached)
    // Execute
    conn.preparedQuery("SELECT * FROM USERS WHERE user_id=$1", Tuple.of("julien"), ar -> {

      if (ar.succeeded()) {

        // Get result
        PgResult<Row> result = ar.result();
      } else {
        System.out.println("Query failed " + ar.cause());
      }
    });
  }

  public void ex4(PgConnection conn) {
    conn.prepare("SELECT * FROM USERS WHERE user_id=$1", ar1 -> {

      if (ar1.succeeded()) {
        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgQuery query = preparedStatement.createQuery(Tuple.of("julien"));

        // Execute query
        query.execute(ar2 -> {
          if (ar2.succeeded()) {

            // Get result
            PgResult<Row> result = ar2.result();
          } else {
            System.out.println("Query failed " + ar2.cause());
          }
        });
      } else {
        System.out.println("Could not prepare statement " + ar1.cause());
      }
    });
  }

  public void ex6(PgConnection conn) {
    conn.prepare("SELECT * FROM USERS", ar1 -> {
      if (ar1.succeeded()) {

        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgQuery query = preparedStatement.createQuery()
          .fetch(100); // Get at most 100 rows at a time

        query.execute(ar2 -> {

          if (ar2.succeeded()) {
            System.out.println("Got at most 100 rows");

            if (query.hasMore()) {
              // Get results
              PgResult<Row> result = ar2.result();

              System.out.println("Get next 100");
              query.execute(ar3 -> {
                // Continue...
              });
            } else {
              // We are done
            }
          } else {
            System.out.println("Query failed " + ar2.cause());
          }
        });
      } else {
        System.out.println("Could not prepare statement " + ar1.cause());
      }
    });
  }

  public void ex7(PgConnection conn) {
    conn.prepare("SELECT * FROM USERS", ar1 -> {

      if (ar1.succeeded()) {
        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgQuery query = preparedStatement.createQuery();

        // Get at most 100 rows
        query.fetch(100);

        // Execute query
        query.execute(res -> {
          if (res.succeeded()) {

            // Get result
            PgResult<Row> result = res.result();

            // Close the query
            query.close();
          } else {
            System.out.println("Query failed " + res.cause());
          }
        });
      } else {
        System.out.println("Could not prepare statement " + ar1.cause());
      }
    });
  }

  public void ex8(PgConnection conn) {

    // Prepare (when not cached)
    // Execute
    conn.preparedQuery("UPDATE USERS SET name=$1 WHERE id=$2", Tuple.of(2, "EMAD ALBLUESHI"), ar -> {

      if(ar.succeeded()) {
        // Process results
        PgResult<Row> result = ar.result();
      } else {
        System.out.println("Update failed " + ar.cause());
      }
    });
  }

  public void ex9(PgConnection conn) {
    conn.prepare("INSERT INTO USERS (id, name) VALUES ($1, $2)", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedStatement preparedStatement = ar1.result();

        // Create a query : bind parameters
        PgBatch batch = preparedStatement.createBatch();

        // Add commands to the createBatch
        batch.add(Tuple.of("julien", "Julien Viet"));
        batch.add(Tuple.of("emad", "Emad Alblueshi"));

        batch.execute(res -> {
          if (res.succeeded()) {

            // Process results
            PgBatchResult<Row> results = res.result();
          } else {
            System.out.println("Batch failed " + res.cause());
          }
        });
      } else {
        System.out.println("Could not prepare statement " + ar1.cause());
      }
    });
  }

  public void ex9_(PgConnection conn) {

    List<Tuple> batch = new ArrayList<>();
    batch.add(Tuple.of("julien", "Julien Viet"));
    batch.add(Tuple.of("emad", "Emad Alblueshi"));

    conn.preparedBatch("INSERT INTO USERS (id, name) VALUES ($1, $2)", batch, ar -> {
      if (ar.succeeded()) {

        // Process results
        PgBatchResult<Row> results = ar.result();
      } else {
        System.out.println("Batch failed " + ar.cause());
      }
    });
  }

  public void ex10(Vertx vertx) {

    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUsername("user")
      .setPassword("secret")
      .setSsl(true)
      .setPemTrustOptions(new PemTrustOptions().addCertPath("/path/to/cert.pem"));

    PgClient.connect(vertx, options, res -> {
      if (res.succeeded()) {
        // Connected with SSL
      } else {
        System.out.println("Could not connect " + res.cause());
      }
    });
  }
}

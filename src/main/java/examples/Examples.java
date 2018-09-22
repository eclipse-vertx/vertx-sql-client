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

import io.reactiverse.pgclient.*;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.Numeric;
import io.reactiverse.pgclient.pubsub.PgSubscriber;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.docgen.Source;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
      .setUser("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the client pool
    PgPool client = PgClient.pool(options);

    // A simple query
    client.query("SELECT * FROM users WHERE id='julien'", ar -> {
      if (ar.succeeded()) {
        PgRowSet result = ar.result();
        System.out.println("Got " + result.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }

      // Now close the pool
      client.close();
    });
  }

  public void configureFromEnv(Vertx vertx) {

    // Create the pool from the environment variables
    PgPool pool = PgClient.pool();

    // Create the connection from the environment variables
    PgClient.connect(vertx, res -> {
      // Handling your connection
    });
  }

  public void configureFromUri(Vertx vertx) {

    // Connection URI
    String connectionUri = "postgresql://dbuser:secretpassword@database.server.com:3211/mydb";

    // Create the pool from the connection URI
    PgPool pool = PgClient.pool(connectionUri);

    // Create the connection from the connection URI
    PgClient.connect(vertx, connectionUri, res -> {
      // Handling your connection
    });
  }

  public void connecting01() {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pooled client
    PgPool client = PgClient.pool(options);
  }

  public void connecting02(Vertx vertx) {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pooled client
    PgPool client = PgClient.pool(vertx, options);
  }

  public void connecting03(PgPool pool) {

    // Close the pool and all the associated resources
    pool.close();
  }

  public void connecting04(Vertx vertx) {

    // Pool options
    PgPoolOptions options = new PgPoolOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret")
      .setMaxSize(5);

    // Create the pooled client
    PgPool client = PgClient.pool(vertx, options);

    // Get a connection from the pool
    client.getConnection(ar1 -> {

      if (ar1.succeeded()) {

        System.out.println("Connected");

        // Obtain our connection
        PgConnection conn = ar1.result();

        // All operations execute on the same connection
        conn.query("SELECT * FROM users WHERE id='julien'", ar2 -> {
          if (ar2.succeeded()) {
            conn.query("SELECT * FROM users WHERE id='emad'", ar3 -> {
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
    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Connect to Postgres
    PgClient.connect(vertx, options, res -> {
      if (res.succeeded()) {

        System.out.println("Connected");

        // Obtain our connection
        PgConnection conn = res.result();

        // All operations execute on the same connection
        conn.query("SELECT * FROM users WHERE id='julien'", ar2 -> {
          if (ar2.succeeded()) {
            conn.query("SELECT * FROM users WHERE id='emad'", ar3 -> {
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

    // Pool Options
    // Socket file name will be /var/run/postgresql/.s.PGSQL.5432
    PgPoolOptions options = new PgPoolOptions()
      .setHost("/var/run/postgresql")
      .setPort(5432)
      .setDatabase("the-db");

    // Create the pooled client
    PgPool client = PgClient.pool(options);

    // Create the pooled client with a vertx instance
    // Make sure the vertx instance has enabled native transports
    PgPool client2 = PgClient.pool(vertx, options);
  }

  public void queries01(PgClient client) {
    client.query("SELECT * FROM users WHERE id='julien'", ar -> {
      if (ar.succeeded()) {
        PgRowSet result = ar.result();
        System.out.println("Got " + result.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries02(PgClient client) {
    client.preparedQuery("SELECT * FROM users WHERE id=$1", Tuple.of("julien"),  ar -> {
      if (ar.succeeded()) {
        PgRowSet rows = ar.result();
        System.out.println("Got " + rows.size() + " rows ");
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries03(PgClient client) {
    client.preparedQuery("SELECT first_name, last_name FROM users", ar -> {
      if (ar.succeeded()) {
        PgRowSet rows = ar.result();
        for (Row row : rows) {
          System.out.println("User " + row.getString(0) + " " + row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void queries04(PgClient client) {
    client.preparedQuery("INSERT INTO users (first_name, last_name) VALUES ($1, $2)", Tuple.of("Julien", "Viet"),  ar -> {
      if (ar.succeeded()) {
        PgRowSet rows = ar.result();
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

  public void queries08(PgClient client) {

    // Add commands to the batch
    List<Tuple> batch = new ArrayList<>();
    batch.add(Tuple.of("julien", "Julien Viet"));
    batch.add(Tuple.of("emad", "Emad Alblueshi"));

    // Execute the prepared batch
    client.preparedBatch("INSERT INTO USERS (id, name) VALUES ($1, $2)", batch, res -> {
      if (res.succeeded()) {

        // Process rows
        PgRowSet rows = res.result();
      } else {
        System.out.println("Batch failed " + res.cause());
      }
    });
  }

  public void queries09(Vertx vertx, PgPoolOptions options) {

    // Enable prepare statements
    options.setCachePreparedStatements(true);

    PgPool client = PgClient.pool(vertx, options);
  }

  public void usingConnections01(Vertx vertx, PgPool pool) {

    pool.getConnection(ar1 -> {
      if (ar1.succeeded()) {
        PgConnection connection = ar1.result();

        connection.query("SELECT * FROM users WHERE id='julien'", ar2 -> {
          if (ar1.succeeded()) {
            connection.query("SELECT * FROM users WHERE id='paulo'", ar3 -> {
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

  public void usingConnections02(PgConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedQuery pq = ar1.result();
        pq.execute(Tuple.of("julien"), ar2 -> {
          if (ar2.succeeded()) {
            // All rows
            PgRowSet rows = ar2.result();
          }
        });
      }
    });
  }

  public void usingConnections03(PgConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedQuery pq = ar1.result();

        // Create a cursor
        PgCursor cursor = pq.cursor(Tuple.of("julien"));

        // Read 50 rows
        cursor.read(50, ar2 -> {
          if (ar2.succeeded()) {
            PgRowSet rows = ar2.result();

            // Check for more ?
            if (cursor.hasMore()) {

              // Read the next 50
              cursor.read(50, ar3 -> {
                // More rows, and so on...
              });
            } else {
              // No more rows
            }
          }
        });
      }
    });
  }

  public void usingConnections04(PgConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedQuery pq = ar1.result();
        PgCursor cursor = pq.cursor(Tuple.of("julien"));
        cursor.read(50, ar2 -> {
          if (ar2.succeeded()) {
            // Close the cursor
            cursor.close();
          }
        });
      }
    });
  }

  public void usingConnections05(PgConnection connection) {
    connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", ar1 -> {
      if (ar1.succeeded()) {
        PgPreparedQuery pq = ar1.result();

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
        PgPreparedQuery prepared = ar1.result();

        // Create a query : bind parameters
        List<Tuple> batch = new ArrayList();

        // Add commands to the createBatch
        batch.add(Tuple.of("julien", "Julien Viet"));
        batch.add(Tuple.of("emad", "Emad Alblueshi"));

        prepared.batch(batch, res -> {
          if (res.succeeded()) {

            // Process rows
            PgRowSet rows = res.result();
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

          // Return the connection to the pool
          conn.close();
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
          if (ar.succeeded()) {

          } else {
            tx.rollback();
            conn.close();
          }
        });
        conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", ar -> {
          // Fails and triggers transaction aborts
        });

        // Attempt to commit the transaction
        tx.commit(ar -> {
          // But transaction abortion fails it

          // Return the connection to the pool
          conn.close();
        });
      }
    });
  }

  public void transaction03(PgPool pool) {

    // Acquire a transaction and begin the transaction
    pool.begin(res -> {
      if (res.succeeded()) {

        // Get the transaction
        PgTransaction tx = res.result();

        // Various statements
        tx.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", ar -> {});
        tx.query("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')", ar -> {});

        // Commit the transaction and return the connection to the pool
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

  public void typeMapping01(PgPool pool) {
    pool.query("SELECT 1::BIGINT \"VAL\"", ar -> {
      PgRowSet rowSet = ar.result();
      Row row = rowSet.iterator().next();

      // Stored as java.lang.Long
      Object value = row.getValue(0);

      // Convert to java.lang.Integer
      Integer intValue = row.getInteger(0);
    });
  }

  public void typeMapping02(PgPool pool) {
    pool.query("SELECT 1::BIGINT \"VAL\"", ar -> {
      PgRowSet rowSet = ar.result();
      Row row = rowSet.iterator().next();

      // Stored as java.lang.Long
      Object value = row.getValue(0);

      // Convert to java.lang.Integer
      Integer intValue = row.getInteger(0);
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
      .setUser("user")
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
      .setUser("user")
      .setPassword("secret")
    );

    subscriber.connect(ar -> {
        if (ar.succeeded()) {
          // Complex channel name - name in PostgreSQL requires a quoted ID
          subscriber.channel("Complex.Channel.Name").handler(payload -> {
            System.out.println("Received " + payload);
          });
          subscriber.channel("Complex.Channel.Name").subscribeHandler(subscribed -> {
        	  subscriber.actualConnection().query(
        			  "NOTIFY \"Complex.Channel.Name\", 'msg'", notified -> {
        		  System.out.println("Notified \"Complex.Channel.Name\"");
        	  });
          });

          // PostgreSQL simple ID's are forced lower-case
          subscriber.channel("simple_channel").handler(payload -> {
              System.out.println("Received " + payload);
          });
          subscriber.channel("simple_channel").subscribeHandler(subscribed -> {
        	  // The following simple channel identifier is forced to lower case
              subscriber.actualConnection().query(
            		"NOTIFY Simple_CHANNEL, 'msg'", notified -> {
          		  System.out.println("Notified simple_channel");
          	  });
          });

          // The following channel name is longer than the current
          // (NAMEDATALEN = 64) - 1 == 63 character limit and will be truncated
          subscriber.channel(
        		  "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbb"
        		  ).handler(payload -> {
              System.out.println("Received " + payload);
          });
        }
      });
  }

  public void pubsub04(Vertx vertx) {

    PgSubscriber subscriber = PgSubscriber.subscriber(vertx, new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
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

  public void ex10(Vertx vertx) {

    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
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

  public void jsonExample() {

    // Create a tuple
    Tuple tuple = Tuple.of(
      Json.create(Json.create(null)),
      Json.create(Json.create(new JsonObject().put("foo", "bar"))),
      Json.create(Json.create(null)));

    // Retrieving json
    Object value = tuple.getJson(0).value(); // Expect null

    //
    value = tuple.getJson(1).value(); // Expect JSON object

    //
    value = tuple.getJson(3).value(); // Expect 3
  }

  public void numericExample(Row row) {
    Numeric numeric = row.getNumeric("value");
    if (numeric.isNaN()) {
      // Handle NaN
    } else {
      BigDecimal value = numeric.bigDecimalValue();
    }
  }

  public void arrayExample() {
    // Create a tuple with a single array
    Tuple tuple = Tuple.of(new String[]{ "a", "tuple", "with", "arrays" });

    // Add a string array to the tuple
    tuple.addStringArray(new String[]{"another", "array"});

    // Get the first array of string
    String[] array = tuple.getStringArray(0);
  }

  public void customType01Example(PgClient client) {
    client.preparedQuery("SELECT address, (address).city FROM address_book WHERE id=$1", Tuple.of(3),  ar -> {
      if (ar.succeeded()) {
        PgRowSet rows = ar.result();
        for (Row row : rows) {
          System.out.println("Full Address " + row.getString(0) + ", City " + row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void customType02Example(PgClient client) {
    client.preparedQuery("INSERT INTO address_book (id, address) VALUES ($1, $2)", Tuple.of(3, "('Anytown', 'Second Ave', false)"),  ar -> {
      if (ar.succeeded()) {
        PgRowSet rows = ar.result();
        System.out.println(rows.rowCount());
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void collector01Example(PgClient client) {

    // Create a collector projecting a row set to a map
    Collector<Row, ?, Map<Long, String>> collector = Collectors.toMap(
      row -> row.getLong("id"),
      row -> row.getString("last_name"));

    // Run the query with the collector
    client.query("SELECT * FROM users",
      collector,
      ar -> {
      if (ar.succeeded()) {
        PgResult<Map<Long, String>> result = ar.result();

        // Get the map created by the collector
        Map<Long, String> map = result.value();
        System.out.println("Got " + map);
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void collector02Example(PgClient client) {

    // Create a collector projecting a row set to a (last_name_1,last_name_2,...)
    Collector<Row, ?, String> collector = Collectors.mapping(
      row -> row.getString("last_name"),
      Collectors.joining(",", "(", ")")
    );

    // Run the query with the collector
    client.query("SELECT * FROM users",
      collector,
      ar -> {
        if (ar.succeeded()) {
          PgResult<String> result = ar.result();

          // Get the string created by the collector
          String list = result.value();
          System.out.println("Got " + list);
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }
}

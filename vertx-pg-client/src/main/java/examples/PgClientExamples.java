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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.docgen.Source;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.SslMode;
import io.vertx.pgclient.pubsub.PgSubscriber;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Source
public class PgClientExamples {

  public void gettingStarted() {

    // Connect options
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the client pool
    SqlClient client = PgBuilder
      .client()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .build();

    // A simple query
    client
      .query("SELECT * FROM users WHERE id='julien'")
      .execute()
      .onComplete(ar -> {
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

  public void configureFromEnv(Vertx vertx) {

    // Create the pool from the environment variables
    Pool pool = PgBuilder.pool()
      .using(vertx)
      .build();

    // Create the connection from the environment variables
    PgConnection.connect(vertx)
      .onComplete(res -> {
        // Handling your connection
      });
  }

  public void configureFromDataObject(Vertx vertx) {

    // Data object
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool Options
    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    // Create the pool from the data object
    Pool pool = PgBuilder.pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

    pool.getConnection()
      .onComplete(ar -> {
        // Handling your connection
      });
  }

  public void configureDefaultSchema() {
    // Data object
    PgConnectOptions connectOptions = new PgConnectOptions();

    // Set the default schema
    Map<String, String> props = new HashMap<>();
    props.put("search_path", "myschema");
    connectOptions.setProperties(props);
  }

  public void configureFromUri(Vertx vertx) {

    // Connection URI
    String connectionUri = "postgresql://dbuser:secretpassword@database.server.com:5432/mydb";

    // Create the pool from the connection URI
    Pool pool = PgBuilder.pool()
      .connectingTo(connectionUri)
      .using(vertx)
      .build();

    // Create the connection from the connection URI
    PgConnection
      .connect(vertx, connectionUri)
      .onComplete(res -> {
        // Handling your connection
      });
  }

  public void connecting01() {

    // Connect options
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    SqlClient client = PgBuilder
      .client()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .build();
  }

  public void connecting02(Vertx vertx) {

    // Connect options
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    SqlClient client = PgBuilder
      .client()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();
  }

  public void connecting03(Pool client) {

    // Close the pooled client and all the associated resources
    client.close();
  }

  public void connecting04(Vertx vertx) {

    // Connect options
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    Pool pool = PgBuilder
      .pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

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

  public void connecting05(Vertx vertx) {

    // Pool options
    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Connect to Postgres
    PgConnection.connect(vertx, options)
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

  public void clientPipelining(Vertx vertx, PgConnectOptions connectOptions, PoolOptions poolOptions) {
    Pool pool = PgBuilder
      .pool()
      .connectingTo(connectOptions.setPipeliningLimit(16))
      .with(poolOptions)
      .using(vertx)
      .build();
  }

  public void poolVersusPooledClient(Vertx vertx, String sql, PgConnectOptions connectOptions, PoolOptions poolOptions) {

    // Pooled client
    SqlClient client = PgBuilder
      .client()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

    // Pipelined
    Future<RowSet<Row>> res1 = client.query(sql).execute();

    // Connection pool
    Pool pool = PgBuilder
      .pool()
      .connectingTo(connectOptions)
      .with(poolOptions)
      .using(vertx)
      .build();

    // Not pipelined
    Future<RowSet<Row>> res2 = pool.query(sql).execute();
  }

  public void unixDomainSockets(Vertx vertx) {

    // Connect Options
    // Socket file name will be /var/run/postgresql/.s.PGSQL.5432
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setHost("/var/run/postgresql")
      .setPort(5432)
      .setDatabase("the-db");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    Pool client = PgBuilder
      .pool()
      .connectingTo(connectOptions)
      .with(poolOptions)
      .build();

    // Create the pooled client with a vertx instance
    // Make sure the vertx instance has enabled native transports
    Pool client2 = PgBuilder
      .pool()
      .connectingTo(connectOptions)
      .with(poolOptions)
      .using(vertx)
      .build();
  }

  public void reconnectAttempts(PgConnectOptions options) {
    // The client will try to connect at most 3 times at a 1 second interval
    options
      .setReconnectAttempts(2)
      .setReconnectInterval(1000);
  }

  public void typeMapping01(Pool pool) {
    pool
      .query("SELECT 1::BIGINT \"VAL\"")
      .execute()
      .onComplete(ar -> {
        RowSet<Row> rowSet = ar.result();
        Row row = rowSet.iterator().next();

        // Stored as java.lang.Long
        Object value = row.getValue(0);

        // Convert to java.lang.Integer
        Integer intValue = row.getInteger(0);
      });
  }

  public void typeMapping02(Pool pool) {
    pool
      .query("SELECT 1::BIGINT \"VAL\"")
      .execute()
      .onComplete(ar -> {
        RowSet<Row> rowSet = ar.result();
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

    connection
      .query("LISTEN some-channel")
      .execute()
      .onComplete(ar -> {
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

    subscriber
      .connect()
      .onComplete(ar -> {
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

    subscriber
      .connect()
      .onComplete(ar -> {
        if (ar.succeeded()) {
          // Complex channel name - name in PostgreSQL requires a quoted ID
          subscriber.channel("Complex.Channel.Name").handler(payload -> {
            System.out.println("Received " + payload);
          });
          subscriber.channel("Complex.Channel.Name").subscribeHandler(subscribed -> {
            subscriber.actualConnection()
              .query("NOTIFY \"Complex.Channel.Name\", 'msg'")
              .execute()
              .onComplete(notified -> {
                System.out.println("Notified \"Complex.Channel.Name\"");
              });
          });

          // PostgreSQL simple ID's are forced lower-case
          subscriber.channel("simple_channel").handler(payload -> {
              System.out.println("Received " + payload);
          });
          subscriber.channel("simple_channel").subscribeHandler(subscribed -> {
            // The following simple channel identifier is forced to lower case
            subscriber.actualConnection()
              .query("NOTIFY Simple_CHANNEL, 'msg'")
              .execute()
              .onComplete(notified -> {
                System.out.println("Notified simple_channel");
              });
          });

          // The following channel name is longer than the current
          // (NAMEDATALEN = 64) - 1 == 63 character limit and will be truncated
          subscriber.channel("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbb")
            .handler(payload -> {
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

  public void noticeHandler(PgConnection connection) {
    connection.noticeHandler(notice -> {
      System.out.println("Received notice " + notice.getSeverity() + "" + notice.getMessage());
    });
  }

  public void ex10(Vertx vertx) {

    PgConnectOptions options = new PgConnectOptions()
      .setPort(5432)
      .setHost("the-host")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret")
      .setSslMode(SslMode.VERIFY_CA)
      .setSslOptions(new ClientSSLOptions().setTrustOptions(new PemTrustOptions().addCertPath("/path/to/cert.pem")));

    PgConnection
      .connect(vertx, options)
      .onComplete(res -> {
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

  public void arrayExample() {
    // Create a tuple with a single array
    Tuple tuple = Tuple.of(new String[]{ "a", "tuple", "with", "arrays" });

    // Add a string array to the tuple
    tuple.addArrayOfString(new String[]{"another", "array"});

    // Get the first array of string
    String[] array = tuple.getArrayOfStrings(0);
  }

  public void infinitySpecialValue(SqlClient client) {
    client
      .query("SELECT 'infinity'::DATE \"LocalDate\"")
      .execute()
      .onComplete(ar -> {
        if (ar.succeeded()) {
          Row row = ar.result().iterator().next();
          System.out.println(row.getLocalDate("LocalDate").equals(LocalDate.MAX));
        } else {
          System.out.println("Failure: " + ar.cause().getMessage());
        }
      });
  }

  public void customType01Example(SqlClient client) {
    client
      .preparedQuery("SELECT address, (address).city FROM address_book WHERE id=$1")
      .execute(Tuple.of(3))
      .onComplete(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        for (Row row : rows) {
          System.out.println("Full Address " + row.getString(0) + ", City " + row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void customType02Example(SqlClient client) {
    client
      .preparedQuery("INSERT INTO address_book (id, address) VALUES ($1, $2)")
      .execute(Tuple.of(3, "('Anytown', 'Second Ave', false)"))
      .onComplete(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        System.out.println(rows.rowCount());
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }


  public void tsQuery01Example(SqlClient client) {
    client
      .preparedQuery("SELECT to_tsvector( $1 ) @@ to_tsquery( $2 )")
      .execute(Tuple.of("fat cats ate fat rats", "fat & rat"))
      .onComplete(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        for (Row row : rows) {
          System.out.println("Match : " + row.getBoolean(0));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void tsQuery02Example(SqlClient client) {
    client
      .preparedQuery("SELECT to_tsvector( $1 ), to_tsquery( $2 )")
      .execute(Tuple.of("fat cats ate fat rats", "fat & rat"))
      .onComplete(ar -> {
      if (ar.succeeded()) {
        RowSet<Row> rows = ar.result();
        for (Row row : rows) {
          System.out.println("Vector : " + row.getString(0) + ", query : "+row.getString(1));
        }
      } else {
        System.out.println("Failure: " + ar.cause().getMessage());
      }
    });
  }

  public void enumeratedType01Example(SqlClient client) {
    client
      .preparedQuery("INSERT INTO colors VALUES ($2)")
      .execute(Tuple.of("red"))
      .onComplete(res -> {
        // ...
      });
  }

  enum Color {
    red
  }

  public void enumType01Example(SqlClient client) {
    client
      .preparedQuery("INSERT INTO colors VALUES ($1)")
      .execute(Tuple.of(Color.red))
      .flatMap(res ->
        client
          .preparedQuery("SELECT color FROM colors")
          .execute()
      ).onComplete(res -> {
        if (res.succeeded()) {
          RowSet<Row> rows = res.result();
          for (Row row : rows) {
            System.out.println(row.get(Color.class, "color"));
          }
        }
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
      .execute()
      .onComplete(ar -> {
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
    client
      .query("SELECT * FROM users")
      .collecting(collector)
      .execute()
      .onComplete(ar -> {
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

  public void cancelRequest(PgConnection connection) {
    connection
      .query("SELECT pg_sleep(20)")
      .execute()
      .onComplete(ar -> {
      if (ar.succeeded()) {
        // imagine this is a long query and is still running
        System.out.println("Query success");
      } else {
        // the server will abort the current query after cancelling request
        System.out.println("Failed to query due to " + ar.cause().getMessage());
      }
    });
    connection
      .cancelRequest()
      .onComplete(ar -> {
      if (ar.succeeded()) {
        System.out.println("Cancelling request has been sent");
      } else {
        System.out.println("Failed to send cancelling request");
      }
    });
  }

  public void insertReturning(SqlClient client) {
    client
      .preparedQuery("INSERT INTO color (color_name) VALUES ($1), ($2), ($3) RETURNING color_id")
      .execute(Tuple.of("white", "red", "blue"))
      .onSuccess(rows -> {
        for (Row row : rows) {
          System.out.println("generated key: " + row.getInteger("color_id"));
        }
    });
  }

  public void deleteReturning(SqlClient client) {
    client
      .query("DELETE FROM color RETURNING color_name")
      .execute()
      .onSuccess(rows -> {
        for (Row row : rows) {
          System.out.println("deleted color: " + row.getString("color_name"));
        }
      });
  }

  public void batchReturning(SqlClient client) {
    client
      .preparedQuery("INSERT INTO color (color_name) VALUES ($1) RETURNING color_id")
      .executeBatch(Arrays.asList(Tuple.of("white"), Tuple.of("red"), Tuple.of("blue")))
      .onSuccess(res -> {
        for (RowSet<Row> rows = res; rows != null; rows = rows.next()) {
          Integer colorId = rows.iterator().next().getInteger("color_id");
          System.out.println("generated key: " + colorId);
        }
      });
  }

  public void pgBouncer(PgConnectOptions connectOptions) {
    connectOptions.setUseLayer7Proxy(true);
  }
}

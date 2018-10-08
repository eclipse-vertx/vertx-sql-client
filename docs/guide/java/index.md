# Reactive Postgres Client

The Reactive Postgres Client is a client for Postgres with a straightforward API focusing on
scalability and low overhead.

The client is reactive and non blocking, allowing to handle many database connections with a single thread.

* Event driven
* Lightweight
* Built-in connection pooling
* Prepared queries caching
* Publish / subscribe using Postgres `NOTIFY/LISTEN`
* Batch and cursor
* Row streaming
* Command pipeling
* RxJava 1 and RxJava 2
* Direct memory to object without unnecessary copies
* Java 8 Date and Time
* SSL/TLS
* Unix domain socket
* HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy support

## Usage

To use the Reactive Postgres Client add the following dependency to the _dependencies_ section of your build descriptor:

* Maven (in your `pom.xml`):

[source,xml,subs#"+attributes"]
```
<dependency>
 <groupId>io.reactiverse</groupId>
 <artifactId>reactive-pg-client</artifactId>
 <version>0.10.6</version>
</dependency>
```

* Gradle (in your `build.gradle` file):

```groovy
dependencies {
 compile 'io.reactiverse:reactive-pg-client:0.10.6'
}
```

## Getting started

Here is the simplest way to connect, query and disconnect

```java
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
```

## Connecting to Postgres

Most of the time you will use a pool to connect to Postgres:

```java
PgPoolOptions options = new PgPoolOptions()
  .setPort(5432)
  .setHost("the-host")
  .setDatabase("the-db")
  .setUser("user")
  .setPassword("secret")
  .setMaxSize(5);

// Create the pooled client
PgPool client = PgClient.pool(options);
```

The pooled client uses a connection pool and any operation will borrow a connection from the pool
to execute the operation and release it to the pool.

If you are running with Vert.x you can pass it your Vertx instance:

```java
PgPoolOptions options = new PgPoolOptions()
  .setPort(5432)
  .setHost("the-host")
  .setDatabase("the-db")
  .setUser("user")
  .setPassword("secret")
  .setMaxSize(5);

// Create the pooled client
PgPool client = PgClient.pool(vertx, options);
```

You need to release the pool when you don't need it anymore:

```java
pool.close();
```

When you need to execute several operations on the same connection, you need to use a client
[`connection`](../../apidocs/io/reactiverse/pgclient/PgConnection.html).

You can easily get one from the pool:

```java
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
```

Once you are done with the connection you must close it to release it to the pool, so it can be reused.

Sometimes you want to improve performance via Unix domain socket connection, we achieve this with Vert.x Native transports.

Make sure you have added the required `netty-transport-native` dependency in your classpath and enabled the Unix domain socket option.

```java
PgPoolOptions options = new PgPoolOptions()
  .setHost("/var/run/postgresql")
  .setPort(5432)
  .setDatabase("the-db");

// Create the pooled client
PgPool client = PgClient.pool(options);

// Create the pooled client with a vertx instance
// Make sure the vertx instance has enabled native transports
PgPool client2 = PgClient.pool(vertx, options);
```

More information can be found in the [Vert.x documentation](https://vertx.io/docs/vertx-core/java/#_native_transports).

## Configuration

There are several options for you to configure the client.

Apart from configuring with a `PgPoolOptions` data object, We also provide you an alternative way to connect when you want to configure with a connection URI:

```java
String connectionUri = "postgresql://dbuser:secretpassword@database.server.com:3211/mydb";

// Create the pool from the connection URI
PgPool pool = PgClient.pool(connectionUri);

// Create the connection from the connection URI
PgClient.connect(vertx, connectionUri, res -> {
  // Handling your connection
});
```

More information about connection string formats can be found in the [PostgreSQL Manuals](https://www.postgresql.org/docs/9.6/static/libpq-connect.html#LIBPQ-CONNSTRING).

You can also use environment variables to set default connection setting values, this is useful
when you want to avoid hard-coding database connection information. You can refer to the [official documentation](https://www.postgresql.org/docs/9.6/static/libpq-envars.html)
for more details. The following parameters are supported:

* `PGHOST`
* `PGHOSTADDR`
* `PGPORT`
* `PGDATABASE`
* `PGUSER`
* `PGPASSWORD`

If you don't specify a data object or a connection URI string to connect, environment variables will take precedence over them.

```
$ PGUSER=user \
 PGHOST=the-host \
 PGPASSWORD=secret \
 PGDATABASE=the-db \
 PGPORT=5432
```

```java
PgPool pool = PgClient.pool();

// Create the connection from the environment variables
PgClient.connect(vertx, res -> {
  // Handling your connection
});
```

## Running queries

When you don't need a transaction or run single queries, you can run queries directly on the pool; the pool
will use one of its connection to run the query and return the result to you.

Here is how to run simple queries:

```java
client.query("SELECT * FROM users WHERE id='julien'", ar -> {
  if (ar.succeeded()) {
    PgRowSet result = ar.result();
    System.out.println("Got " + result.size() + " rows ");
  } else {
    System.out.println("Failure: " + ar.cause().getMessage());
  }
});
```

You can do the same with prepared queries.

The SQL string can refer to parameters by position, using `$1`, `$2`, etc…​

```java
client.preparedQuery("SELECT * FROM users WHERE id=$1", Tuple.of("julien"),  ar -> {
  if (ar.succeeded()) {
    PgRowSet rows = ar.result();
    System.out.println("Got " + rows.size() + " rows ");
  } else {
    System.out.println("Failure: " + ar.cause().getMessage());
  }
});
```

Query methods provides an asynchronous [`PgRowSet`](../../apidocs/io/reactiverse/pgclient/PgRowSet.html) instance that works for _SELECT_ queries

```java
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
```

or _UPDATE_/_INSERT_ queries:

```java
client.preparedQuery("INSERT INTO users (first_name, last_name) VALUES ($1, $2)", Tuple.of("Julien", "Viet"),  ar -> {
  if (ar.succeeded()) {
    PgRowSet rows = ar.result();
    System.out.println(rows.rowCount());
  } else {
    System.out.println("Failure: " + ar.cause().getMessage());
  }
});
```

The [`Row`](../../apidocs/io/reactiverse/pgclient/Row.html) gives you access to your data by index

```java
System.out.println("User " + row.getString(0) + " " + row.getString(1));
```

or by name

```java
System.out.println("User " + row.getString("first_name") + " " + row.getString("last_name"));
```

You can access a wide variety of of types

```java
String firstName = row.getString("first_name");
Boolean male = row.getBoolean("male");
Integer age = row.getInteger("age");
```

You can execute prepared batch

```java
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
```

You can cache prepared queries:

```java
options.setCachePreparedStatements(true);

PgPool client = PgClient.pool(vertx, options);
```

## Using connections

When you need to execute sequential queries (without a transaction), you can create a new connection
or borrow one from the pool:

```java
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
```

Prepared queries can be created:

```java
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
```

NOTE: prepared query caching depends on the [`setCachePreparedStatements`](../../apidocs/io/reactiverse/pgclient/PgConnectOptions.html#setCachePreparedStatements-boolean-) and
does not depend on whether you are creating prepared queries or use [`direct prepared queries`](../../apidocs/io/reactiverse/pgclient/PgClient.html#preparedQuery-java.lang.String-io.vertx.core.Handler-)

By default prepared query executions fetch all rows, you can use a [`PgCursor`](../../apidocs/io/reactiverse/pgclient/PgCursor.html) to control the amount of rows you want to read:

```java
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
```

Cursors shall be closed when they are released prematurely:

```java
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
```

A stream API is also available for cursors, which can be more convenient, specially with the Rxified version.

```java
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
```

The stream read the rows by batch of `50` and stream them, when the rows have been passed to the handler,
a new batch of `50` is read and so on.

The stream can be resumed or paused, the loaded rows will remain in memory until they are delivered and the cursor
will stop iterating.

[`PgPreparedQuery`](../../apidocs/io/reactiverse/pgclient/PgPreparedQuery.html)can perform efficient batching:

```java
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
```

## Using transactions

### Transactions with connections

You can execute transaction using SQL `BEGIN`/`COMMIT`/`ROLLBACK`, if you do so you must use
a [`PgConnection`](../../apidocs/io/reactiverse/pgclient/PgConnection.html) and manage it yourself.

Or you can use the transaction API of [`PgConnection`](../../apidocs/io/reactiverse/pgclient/PgConnection.html):

```java
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
```

When Postgres reports the current transaction is failed (e.g the infamous _current transaction is aborted, commands ignored until
end of transaction block_), the transaction is rollbacked and the [`abortHandler`](../../apidocs/io/reactiverse/pgclient/PgTransaction.html#abortHandler-io.vertx.core.Handler-)
is called:

```java
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
```

### Simplified transaction API

When you use a pool, you can start a transaction directly on the pool.

It borrows a connection from the pool, begins the transaction and releases the connection to the pool when the transaction ends.

```java
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
```

## Postgres type mapping

Currently the client supports the following Postgres types

* BOOLEAN (`java.lang.Boolean`)
* INT2 (`java.lang.Short`)
* INT4 (`java.lang.Integer`)
* INT8 (`java.lang.Long`)
* FLOAT4 (`java.lang.Float`)
* FLOAT8 (`java.lang.Double`)
* CHAR (`java.lang.String`)
* VARCHAR (`java.lang.String`)
* TEXT (`java.lang.String`)
* ENUM (`java.lang.String`)
* NAME (`java.lang.String`)
* NUMERIC (`io.reactiverse.pgclient.data.Numeric`)
* UUID (`java.util.UUID`)
* DATE (`java.time.LocalDate`)
* TIME (`java.time.LocalTime`)
* TIMETZ (`java.time.OffsetTime`)
* TIMESTAMP (`java.time.LocalDateTime`)
* TIMESTAMPTZ (`java.time.OffsetDateTime`)
* INTERVAL (`io.reactiverse.pgclient.data.Interval`)
* BYTEA (`io.vertx.core.buffer.Buffer`)
* JSON (`io.reactiverse.pgclient.data.Json`)
* JSONB (`io.reactiverse.pgclient.data.Json`)
* POINT (`io.reactiverse.pgclient.data.Point`)

Tuple decoding uses the above types when storing values, it also performs on the flu conversion the actual value when possible:

```java
pool.query("SELECT 1::BIGINT \"VAL\"", ar -> {
  PgRowSet rowSet = ar.result();
  Row row = rowSet.iterator().next();

  // Stored as java.lang.Long
  Object value = row.getValue(0);

  // Convert to java.lang.Integer
  Integer intValue = row.getInteger(0);
});
```

Tuple encoding uses the above type mapping for encoding, unless the type is numeric in which case `java.lang.Number` is used instead:

```java
pool.query("SELECT 1::BIGINT \"VAL\"", ar -> {
  PgRowSet rowSet = ar.result();
  Row row = rowSet.iterator().next();

  // Stored as java.lang.Long
  Object value = row.getValue(0);

  // Convert to java.lang.Integer
  Integer intValue = row.getInteger(0);
});
```

Arrays of these types are supported.

### Handling JSON

The [`Json`](../../apidocs/io/reactiverse/pgclient/data/Json.html) Java type is used to represent the Postgres `JSON` and `JSONB` type.

The main reason of this type is handling `null` JSON values.

```java
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
```

### Handling NUMERIC

The [`Numeric`](../../apidocs/io/reactiverse/pgclient/data/Numeric.html) Java type is used to represent the Postgres `NUMERIC` type.

```java
Numeric numeric = row.getNumeric("value");
if (numeric.isNaN()) {
  // Handle NaN
} else {
  BigDecimal value = numeric.bigDecimalValue();
}
```

## Handling arrays

Arrays are available on [`Tuple`](../../apidocs/io/reactiverse/pgclient/Tuple.html) and [`Row`](../../apidocs/io/reactiverse/pgclient/Row.html):

```java
Tuple tuple = Tuple.of(new String[]{ "a", "tuple", "with", "arrays" });

// Add a string array to the tuple
tuple.addStringArray(new String[]{"another", "array"});

// Get the first array of string
String[] array = tuple.getStringArray(0);
```

## Handling custom types

Strings are used to represent custom types, both sent to and returned from Postgres.

You can read from Postgres and get the custom type as a string

```java
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
```

You can also write to Postgres by providing a string

```java
client.preparedQuery("INSERT INTO address_book (id, address) VALUES ($1, $2)", Tuple.of(3, "('Anytown', 'Second Ave', false)"),  ar -> {
  if (ar.succeeded()) {
    PgRowSet rows = ar.result();
    System.out.println(rows.rowCount());
  } else {
    System.out.println("Failure: " + ar.cause().getMessage());
  }
});
```

## Collector queries

You can use Java collectors with the query API:

```java
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
```

The collector processing must not keep a reference on the [`Row`](../../apidocs/io/reactiverse/pgclient/Row.html) as
there is a single row used for processing the entire set.

The Java `Collectors` provides many interesting predefined collectors, for example you can
create easily create a string directly from the row set:

```java
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
```

## RxJava support

The rxified API supports RxJava 1 and RxJava 2, the following examples use RxJava 2.

Most asynchronous constructs are available as methods prefixed by `rx`:

```java
Single<PgRowSet> single = pool.rxQuery("SELECT * FROM users WHERE id='julien'");

// Execute the query
single.subscribe(result -> {
  System.out.println("Got " + result.size() + " rows ");
}, err -> {
  System.out.println("Failure: " + err.getMessage());
});
```


### Streaming

RxJava 2 supports `Observable` and `Flowable` types, these are exposed using
the [`PgStream`](../../apidocs/io/reactiverse/reactivex/pgclient/PgStream.html) that you can get
from a [`PgPreparedQuery`](../../apidocs/io/reactiverse/reactivex/pgclient/PgPreparedQuery.html):

```java
Observable<Row> observable = pool.rxGetConnection()
  .flatMapObservable(conn -> conn
    .rxPrepare("SELECT * FROM users WHERE first_name LIKE $1")
    .flatMapObservable(pq -> {
      // Fetch 50 rows at a time
      PgStream<Row> stream = pq.createStream(50, Tuple.of("julien"));
      return stream.toObservable();
    })
    // Close the connection after usage
    .doAfterTerminate(conn::close));

// Then subscribe
observable.subscribe(row -> {
  System.out.println("User: " + row.getString("last_name"));
}, err -> {
  System.out.println("Error: " + err.getMessage());
}, () -> {
  System.out.println("End of stream");
});
```

The same example using `Flowable`:

```java
Flowable<Row> flowable = pool.rxGetConnection()
  .flatMapPublisher(conn -> conn.rxPrepare("SELECT * FROM users WHERE first_name LIKE $1")
    .flatMapPublisher(pq -> {
      // Fetch 50 rows at a time
      PgStream<Row> stream = pq.createStream(50, Tuple.of("julien"));
      return stream.toFlowable();
    }));

// Then subscribe
flowable.subscribe(new Subscriber<Row>() {

  private Subscription sub;

  @Override
  public void onSubscribe(Subscription subscription) {
    sub = subscription;
    subscription.request(1);
  }

  @Override
  public void onNext(Row row) {
    sub.request(1);
    System.out.println("User: " + row.getString("last_name"));
  }

  @Override
  public void onError(Throwable err) {
    System.out.println("Error: " + err.getMessage());
  }

  @Override
  public void onComplete() {
    System.out.println("End of stream");
  }
});
```

### Transaction

The simplified transaction API allows to easily write transactional
asynchronous flows:

```java
Completable completable = pool
  .rxBegin()
  .flatMapCompletable(tx -> tx
    .rxQuery("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')")
    .flatMap(result -> tx.rxQuery("INSERT INTO Users (first_name,last_name) VALUES ('Emad','Alblueshi')"))
    .flatMapCompletable(result -> tx.rxCommit()));

completable.subscribe(() -> {
  // Transaction succeeded
}, err -> {
  // Transaction failed
});
```

## Pub/sub

Postgres supports pub/sub communication channels.

You can set a [`notificationHandler`](../../apidocs/io/reactiverse/pgclient/PgConnection.html#notificationHandler-io.vertx.core.Handler-) to receive
Postgres notifications:

```java
connection.notificationHandler(notification -> {
  System.out.println("Received " + notification.getPayload() + " on channel " + notification.getChannel());
});

connection.query("LISTEN some-channel", ar -> {
  System.out.println("Subscribed to channel");
});
```

The [`PgSubscriber`](../../apidocs/io/reactiverse/pgclient/pubsub/PgSubscriber.html) is a channel manager managing a single connection that
provides per channel subscription:

```java
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
```

The channel name that is given to the channel method will be the exact name of the channel as held by Postgres for sending
notifications.  Note this is different than the representation of the channel name in SQL, and
internally [`PgSubscriber`](../../apidocs/io/reactiverse/pgclient/pubsub/PgSubscriber.html) will prepare the submitted channel name as a quoted identifier:

```java
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
```
You can provide a reconnect policy as a function that takes the number of `retries` as argument and returns an `amountOfTime`
value:

* when `amountOfTime < 0`: the subscriber is closed and there is no retry
* when `amountOfTime = 0`: the subscriber retries to connect immediately
* when `amountOfTime > 0`: the subscriber retries after `amountOfTime` milliseconds

```java
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
```

The default policy is to not reconnect.

## Using SSL/TLS

To configure the client to use SSL connection, you can configure the [`PgConnectOptions`](../../apidocs/io/reactiverse/pgclient/PgConnectOptions.html)
like a Vert.x `NetClient`.

```java
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
```

More information can be found in the [Vert.x documentation](http://vertx.io/docs/vertx-core/java/#ssl).

## Using a proxy

You can also configure the client to use an HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy.

More information can be found in the [Vert.x documentation](http://vertx.io/docs/vertx-core/java/#_using_a_proxy_for_client_connections).
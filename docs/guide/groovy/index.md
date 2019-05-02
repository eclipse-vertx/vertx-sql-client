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

```xml
<dependency>
 <groupId>io.reactiverse</groupId>
 <artifactId>reactive-pg-client</artifactId>
 <version>0.11.2</version>
</dependency>
```

* Gradle (in your `build.gradle` file):

```groovy
dependencies {
 compile 'io.reactiverse:reactive-pg-client:0.11.2'
}
```

## Getting started

Here is the simplest way to connect, query and disconnect

```groovy

// Pool options
def options = [
  port:5432,
  host:"the-host",
  database:"the-db",
  user:"user",
  password:"secret",
  maxSize:5
]

// Create the client pool
def client = PgClient.pool(options)

// A simple query
client.query("SELECT * FROM users WHERE id='julien'", { ar ->
  if (ar.succeeded()) {
    def result = ar.result()
    println("Got ${result.size()} rows ")
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }

  // Now close the pool
  client.close()
})

```

## Connecting to Postgres

Most of the time you will use a pool to connect to Postgres:

```groovy

// Pool options
def options = [
  port:5432,
  host:"the-host",
  database:"the-db",
  user:"user",
  password:"secret",
  maxSize:5
]

// Create the pooled client
def client = PgClient.pool(options)

```

The pooled client uses a connection pool and any operation will borrow a connection from the pool
to execute the operation and release it to the pool.

If you are running with Vert.x you can pass it your Vertx instance:

```groovy

// Pool options
def options = [
  port:5432,
  host:"the-host",
  database:"the-db",
  user:"user",
  password:"secret",
  maxSize:5
]

// Create the pooled client
def client = PgClient.pool(vertx, options)

```

You need to release the pool when you don't need it anymore:

```groovy

// Close the pool and all the associated resources
pool.close()

```

When you need to execute several operations on the same connection, you need to use a client
[`connection`](../../apidocs/io/reactiverse/pgclient/PgConnection.html).

You can easily get one from the pool:

```groovy

// Pool options
def options = [
  port:5432,
  host:"the-host",
  database:"the-db",
  user:"user",
  password:"secret",
  maxSize:5
]

// Create the pooled client
def client = PgClient.pool(vertx, options)

// Get a connection from the pool
client.getConnection({ ar1 ->

  if (ar1.succeeded()) {

    println("Connected")

    // Obtain our connection
    def conn = ar1.result()

    // All operations execute on the same connection
    conn.query("SELECT * FROM users WHERE id='julien'", { ar2 ->
      if (ar2.succeeded()) {
        conn.query("SELECT * FROM users WHERE id='emad'", { ar3 ->
          // Release the connection to the pool
          conn.close()
        })
      } else {
        // Release the connection to the pool
        conn.close()
      }
    })
  } else {
    println("Could not connect: ${ar1.cause().getMessage()}")
  }
})

```

Once you are done with the connection you must close it to release it to the pool, so it can be reused.

Sometimes you want to improve performance via Unix domain socket connection, we achieve this with Vert.x Native transports.

Make sure you have added the required `netty-transport-native` dependency in your classpath and enabled the Unix domain socket option.

```groovy

// Pool Options
// Socket file name will be /var/run/postgresql/.s.PGSQL.5432
def options = [
  host:"/var/run/postgresql",
  port:5432,
  database:"the-db"
]

// Create the pooled client
def client = PgClient.pool(options)

// Create the pooled client with a vertx instance
// Make sure the vertx instance has enabled native transports
def client2 = PgClient.pool(vertx, options)

```

More information can be found in the [Vert.x documentation](https://vertx.io/docs/vertx-core/java/#_native_transports).

## Configuration

There are several options for you to configure the client.

Apart from configuring with a `PgPoolOptions` data object, We also provide you an alternative way to connect when you want to configure with a connection URI:

```groovy

// Connection URI
def connectionUri = "postgresql://dbuser:secretpassword@database.server.com:3211/mydb"

// Create the pool from the connection URI
def pool = PgClient.pool(connectionUri)

// Create the connection from the connection URI
PgClient.connect(vertx, connectionUri, { res ->
  // Handling your connection
})

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
* `PGSSLMODE`

If you don't specify a data object or a connection URI string to connect, environment variables will take precedence over them.

```
$ PGUSER=user \
 PGHOST=the-host \
 PGPASSWORD=secret \
 PGDATABASE=the-db \
 PGPORT=5432 \
 PGSSLMODE=DISABLE
```

```groovy

// Create the pool from the environment variables
def pool = PgClient.pool()

// Create the connection from the environment variables
PgClient.connect(vertx, { res ->
  // Handling your connection
})

```

## Running queries

When you don't need a transaction or run single queries, you can run queries directly on the pool; the pool
will use one of its connection to run the query and return the result to you.

Here is how to run simple queries:

```groovy
client.query("SELECT * FROM users WHERE id='julien'", { ar ->
  if (ar.succeeded()) {
    def result = ar.result()
    println("Got ${result.size()} rows ")
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }
})

```

You can do the same with prepared queries.

The SQL string can refer to parameters by position, using `$1`, `$2`, etc…​

```groovy
client.preparedQuery("SELECT * FROM users WHERE id=$1", Tuple.of("julien"), { ar ->
  if (ar.succeeded()) {
    def rows = ar.result()
    println("Got ${rows.size()} rows ")
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }
})

```

Query methods provides an asynchronous [`PgRowSet`](../../apidocs/io/reactiverse/pgclient/PgRowSet.html) instance that works for _SELECT_ queries

```groovy
client.preparedQuery("SELECT first_name, last_name FROM users", { ar ->
  if (ar.succeeded()) {
    def rows = ar.result()
    rows.each { row ->
      println("User ${row.getString(0)} ${row.getString(1)}")
    }
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }
})

```

or _UPDATE_/_INSERT_ queries:

```groovy
client.preparedQuery("INSERT INTO users (first_name, last_name) VALUES ($1, $2)", Tuple.of("Julien", "Viet"), { ar ->
  if (ar.succeeded()) {
    def rows = ar.result()
    println(rows.rowCount())
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }
})

```

The [`Row`](../../apidocs/io/reactiverse/pgclient/Row.html) gives you access to your data by index

```groovy
println("User ${row.getString(0)} ${row.getString(1)}")

```

or by name

```groovy
println("User ${row.getString("first_name")} ${row.getString("last_name")}")

```

You can access a wide variety of of types

```groovy

def firstName = row.getString("first_name")
def male = row.getBoolean("male")
def age = row.getInteger("age")

// ...


```

You can execute prepared batch

```groovy

// Add commands to the batch
def batch = []
batch.add(Tuple.of("julien", "Julien Viet"))
batch.add(Tuple.of("emad", "Emad Alblueshi"))

// Execute the prepared batch
client.preparedBatch("INSERT INTO USERS (id, name) VALUES ($1, $2)", batch, { res ->
  if (res.succeeded()) {

    // Process rows
    def rows = res.result()
  } else {
    println("Batch failed ${res.cause()}")
  }
})

```

You can cache prepared queries:

```groovy

// Enable prepare statements
options.cachePreparedStatements = true

def client = PgClient.pool(vertx, options)

```

You can fetch generated keys with a 'RETURNING' clause in your query:

```groovy
client.preparedQuery("INSERT INTO color (color_name) VALUES ($1), ($2), ($3) RETURNING color_id", Tuple.of("white", "red", "blue"), { ar ->
  if (ar.succeeded()) {
    def rows = ar.result()
    println(rows.rowCount())
    rows.each { row ->
      println("generated key: ${row.getInteger("color_id")}")
    }
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }
})

```

## Using connections

When you need to execute sequential queries (without a transaction), you can create a new connection
or borrow one from the pool:

```groovy
Code not translatable
```

Prepared queries can be created:

```groovy
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", { ar1 ->
  if (ar1.succeeded()) {
    def pq = ar1.result()
    pq.execute(Tuple.of("julien"), { ar2 ->
      if (ar2.succeeded()) {
        // All rows
        def rows = ar2.result()
      }
    })
  }
})

```

NOTE: prepared query caching depends on the [`setCachePreparedStatements`](../../apidocs/io/reactiverse/pgclient/PgConnectOptions.html#setCachePreparedStatements-boolean-) and
does not depend on whether you are creating prepared queries or use [`direct prepared queries`](../../apidocs/io/reactiverse/pgclient/PgClient.html#preparedQuery-java.lang.String-io.vertx.core.Handler-)

[`PgPreparedQuery`](../../apidocs/io/reactiverse/pgclient/PgPreparedQuery.html)can perform efficient batching:

```groovy
connection.prepare("INSERT INTO USERS (id, name) VALUES ($1, $2)", { ar1 ->
  if (ar1.succeeded()) {
    def prepared = ar1.result()

    // Create a query : bind parameters
    def batch = []

    // Add commands to the createBatch
    batch.add(Tuple.of("julien", "Julien Viet"))
    batch.add(Tuple.of("emad", "Emad Alblueshi"))

    prepared.batch(batch, { res ->
      if (res.succeeded()) {

        // Process rows
        def rows = res.result()
      } else {
        println("Batch failed ${res.cause()}")
      }
    })
  }
})

```


## Using transactions

### Transactions with connections

You can execute transaction using SQL `BEGIN`/`COMMIT`/`ROLLBACK`, if you do so you must use
a [`PgConnection`](../../apidocs/io/reactiverse/pgclient/PgConnection.html) and manage it yourself.

Or you can use the transaction API of [`PgConnection`](../../apidocs/io/reactiverse/pgclient/PgConnection.html):

```groovy
Code not translatable
```

When Postgres reports the current transaction is failed (e.g the infamous _current transaction is aborted, commands ignored until
end of transaction block_), the transaction is rollbacked and the [`abortHandler`](../../apidocs/io/reactiverse/pgclient/PgTransaction.html#abortHandler-io.vertx.core.Handler-)
is called:

```groovy
pool.getConnection({ res ->
  if (res.succeeded()) {

    // Transaction must use a connection
    def conn = res.result()

    // Begin the transaction
    def tx = conn.begin().abortHandler({ v ->
      println("Transaction failed => rollbacked")
    })

    conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", { ar ->
      // Works fine of course
      if (ar.succeeded()) {

      } else {
        tx.rollback()
        conn.close()
      }
    })
    conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", { ar ->
      // Fails and triggers transaction aborts
    })

    // Attempt to commit the transaction
    tx.commit({ ar ->
      // But transaction abortion fails it

      // Return the connection to the pool
      conn.close()
    })
  }
})

```

### Simplified transaction API

When you use a pool, you can start a transaction directly on the pool.

It borrows a connection from the pool, begins the transaction and releases the connection to the pool when the transaction ends.

```groovy
Code not translatable
```

## Cursors and streaming

By default prepared query execution fetches all rows, you can use a
[`PgCursor`](../../apidocs/io/reactiverse/pgclient/PgCursor.html)to control the amount of rows you want to read:

```groovy
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", { ar1 ->
  if (ar1.succeeded()) {
    def pq = ar1.result()

    // Cursors require to run within a transaction
    def tx = connection.begin()

    // Create a cursor
    def cursor = pq.cursor(Tuple.of("julien"))

    // Read 50 rows
    cursor.read(50, { ar2 ->
      if (ar2.succeeded()) {
        def rows = ar2.result()

        // Check for more ?
        if (cursor.hasMore()) {
          // Repeat the process...
        } else {
          // No more rows - commit the transaction
          tx.commit()
        }
      }
    })
  }
})

```

PostreSQL destroys cursors at the end of a transaction, so the cursor API shall be used
within a transaction, otherwise you will likely get the `34000` PostgreSQL error.

Cursors shall be closed when they are released prematurely:

```groovy
cursor.read(50, { ar2 ->
  if (ar2.succeeded()) {
    // Close the cursor
    cursor.close()
  }
})

```

A stream API is also available for cursors, which can be more convenient, specially with the Rxified version.

```groovy
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", { ar1 ->
  if (ar1.succeeded()) {
    def pq = ar1.result()

    // Streams require to run within a transaction
    def tx = connection.begin()

    // Fetch 50 rows at a time
    def stream = pq.createStream(50, Tuple.of("julien"))

    // Use the stream
    stream.exceptionHandler({ err ->
      println("Error: ${err.getMessage()}")
    })
    stream.endHandler({ v ->
      tx.commit()
      println("End of stream")
    })
    stream.handler({ row ->
      println("User: ${row.getString("last_name")}")
    })
  }
})

```

The stream read the rows by batch of `50` and stream them, when the rows have been passed to the handler,
a new batch of `50` is read and so on.

The stream can be resumed or paused, the loaded rows will remain in memory until they are delivered and the cursor
will stop iterating.

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
* SERIAL2 (`java.lang.Short`)
* SERIAL4 (`java.lang.Integer`)
* SERIAL8 (`java.lang.Long`)
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
* LINE (`io.reactiverse.pgclient.data.Line`)
* LSEG (`io.reactiverse.pgclient.data.LineSegment`)
* BOX (`io.reactiverse.pgclient.data.Box`)
* PATH (`io.reactiverse.pgclient.data.Path`)
* POLYGON (`io.reactiverse.pgclient.data.Polygon`)
* CIRCLE (`io.reactiverse.pgclient.data.Circle`)

Tuple decoding uses the above types when storing values, it also performs on the flu conversion the actual value when possible:

```groovy
pool.query("SELECT 1::BIGINT \"VAL\"", { ar ->
  def rowSet = ar.result()
  def row = rowSet.iterator().next()

  // Stored as java.lang.Long
  def value = row.getValue(0)

  // Convert to java.lang.Integer
  def intValue = row.getInteger(0)
})

```

Tuple encoding uses the above type mapping for encoding, unless the type is numeric in which case `java.lang.Number` is used instead:

```groovy
pool.query("SELECT 1::BIGINT \"VAL\"", { ar ->
  def rowSet = ar.result()
  def row = rowSet.iterator().next()

  // Stored as java.lang.Long
  def value = row.getValue(0)

  // Convert to java.lang.Integer
  def intValue = row.getInteger(0)
})


```

Arrays of these types are supported.

### Handling JSON

The [`Json`](../../apidocs/io/reactiverse/pgclient/data/Json.html) Java type is used to represent the Postgres `JSON` and `JSONB` type.

The main reason of this type is handling `null` JSON values.

```groovy

// Create a tuple
def tuple = Tuple.of(Json.create(Json.create(null)), Json.create(Json.create([
  foo:"bar"
])), Json.create(Json.create(null)))

// Retrieving json
def value = tuple.getJson(0).value()

//
value = tuple.getJson(1).value()

//
value = tuple.getJson(3).value()

```

### Handling NUMERIC

The [`Numeric`](../../apidocs/io/reactiverse/pgclient/data/Numeric.html) Java type is used to represent the Postgres `NUMERIC` type.

```groovy
def numeric = row.getNumeric("value")
if (numeric.isNaN()) {
  // Handle NaN
} else {
  def value = numeric.bigDecimalValue()
}

```

## Handling arrays

Arrays are available on [`Tuple`](../../apidocs/io/reactiverse/pgclient/Tuple.html) and [`Row`](../../apidocs/io/reactiverse/pgclient/Row.html):

```groovy
Code not translatable
```

## Handling custom types

Strings are used to represent custom types, both sent to and returned from Postgres.

You can read from Postgres and get the custom type as a string

```groovy
client.preparedQuery("SELECT address, (address).city FROM address_book WHERE id=$1", Tuple.of(3), { ar ->
  if (ar.succeeded()) {
    def rows = ar.result()
    rows.each { row ->
      println("Full Address ${row.getString(0)}, City ${row.getString(1)}")
    }
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }
})

```

You can also write to Postgres by providing a string

```groovy
client.preparedQuery("INSERT INTO address_book (id, address) VALUES ($1, $2)", Tuple.of(3, "('Anytown', 'Second Ave', false)"), { ar ->
  if (ar.succeeded()) {
    def rows = ar.result()
    println(rows.rowCount())
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }
})

```

## Collector queries

You can use Java collectors with the query API:

```groovy
Code not translatable
```

The collector processing must not keep a reference on the [`Row`](../../apidocs/io/reactiverse/pgclient/Row.html) as
there is a single row used for processing the entire set.

The Java `Collectors` provides many interesting predefined collectors, for example you can
create easily create a string directly from the row set:

```groovy
Code not translatable
```

## RxJava support

The rxified API supports RxJava 1 and RxJava 2, the following examples use RxJava 2.

Most asynchronous constructs are available as methods prefixed by `rx`:

```groovy
Code not translatable
```


### Streaming

RxJava 2 supports `Observable` and `Flowable` types, these are exposed using
the [`PgStream`](../../apidocs/io/reactiverse/reactivex/pgclient/PgStream.html) that you can get
from a [`PgPreparedQuery`](../../apidocs/io/reactiverse/reactivex/pgclient/PgPreparedQuery.html):

```groovy
Code not translatable
```

The same example using `Flowable`:

```groovy
Code not translatable
```

### Transaction

The simplified transaction API allows to easily write transactional
asynchronous flows:

```groovy
Code not translatable
```

## Pub/sub

Postgres supports pub/sub communication channels.

You can set a [`notificationHandler`](../../apidocs/io/reactiverse/pgclient/PgConnection.html#notificationHandler-io.vertx.core.Handler-) to receive
Postgres notifications:

```groovy

connection.notificationHandler({ notification ->
  println("Received ${notification.payload} on channel ${notification.channel}")
})

connection.query("LISTEN some-channel", { ar ->
  println("Subscribed to channel")
})

```

The [`PgSubscriber`](../../apidocs/io/reactiverse/pgclient/pubsub/PgSubscriber.html) is a channel manager managing a single connection that
provides per channel subscription:

```groovy

def subscriber = PgSubscriber.subscriber(vertx, [
  port:5432,
  host:"the-host",
  database:"the-db",
  user:"user",
  password:"secret"
])

// You can set the channel before connect
subscriber.channel("channel1").handler({ payload ->
  println("Received ${payload}")
})

subscriber.connect({ ar ->
  if (ar.succeeded()) {

    // Or you can set the channel after connect
    subscriber.channel("channel2").handler({ payload ->
      println("Received ${payload}")
    })
  }
})

```

The channel name that is given to the channel method will be the exact name of the channel as held by Postgres for sending
notifications.  Note this is different than the representation of the channel name in SQL, and
internally [`PgSubscriber`](../../apidocs/io/reactiverse/pgclient/pubsub/PgSubscriber.html) will prepare the submitted channel name as a quoted identifier:

```groovy

def subscriber = PgSubscriber.subscriber(vertx, [
  port:5432,
  host:"the-host",
  database:"the-db",
  user:"user",
  password:"secret"
])

subscriber.connect({ ar ->
  if (ar.succeeded()) {
    // Complex channel name - name in PostgreSQL requires a quoted ID
    subscriber.channel("Complex.Channel.Name").handler({ payload ->
      println("Received ${payload}")
    })
    subscriber.channel("Complex.Channel.Name").subscribeHandler({ subscribed ->
      subscriber.actualConnection().query("NOTIFY \"Complex.Channel.Name\", 'msg'", { notified ->
        println("Notified \"Complex.Channel.Name\"")
      })
    })

    // PostgreSQL simple ID's are forced lower-case
    subscriber.channel("simple_channel").handler({ payload ->
      println("Received ${payload}")
    })
    subscriber.channel("simple_channel").subscribeHandler({ subscribed ->
      // The following simple channel identifier is forced to lower case
      subscriber.actualConnection().query("NOTIFY Simple_CHANNEL, 'msg'", { notified ->
        println("Notified simple_channel")
      })
    })

    // The following channel name is longer than the current
    // (NAMEDATALEN = 64) - 1 == 63 character limit and will be truncated
    subscriber.channel("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbb").handler({ payload ->
      println("Received ${payload}")
    })
  }
})

```
You can provide a reconnect policy as a function that takes the number of `retries` as argument and returns an `amountOfTime`
value:

* when `amountOfTime < 0`: the subscriber is closed and there is no retry
* when `amountOfTime = 0`: the subscriber retries to connect immediately
* when `amountOfTime > 0`: the subscriber retries after `amountOfTime` milliseconds

```groovy

def subscriber = PgSubscriber.subscriber(vertx, [
  port:5432,
  host:"the-host",
  database:"the-db",
  user:"user",
  password:"secret"
])

// Reconnect at most 10 times after 100 ms each
subscriber.reconnectPolicy({ retries ->
  if (retries < 10) {
    return 100L
  } else {
    return -1L
  }
})

```

The default policy is to not reconnect.

## Cancelling Request

Postgres supports cancellation of requests in progress. You can cancel inflight requests using [`cancelRequest`](../../apidocs/io/reactiverse/pgclient/PgConnection.html#cancelRequest-io.vertx.core.Handler-). Cancelling a request opens a new connection to the server and cancels the request and then close the connection.

```groovy
connection.query("SELECT pg_sleep(20)", { ar ->
  if (ar.succeeded()) {
    // imagine this is a long query and is still running
    println("Query success")
  } else {
    // the server will abort the current query after cancelling request
    println("Failed to query due to ${ar.cause().getMessage()}")
  }
})
connection.cancelRequest({ ar ->
  if (ar.succeeded()) {
    println("Cancelling request has been sent")
  } else {
    println("Failed to send cancelling request")
  }
})

```

> The cancellation signal might or might not have any effect — for example, if it arrives after the backend has finished processing the query, then it will have no effect. If the cancellation is effective, it results in the current command being terminated early with an error message.

More information can be found in the [official documentation](https://www.postgresql.org/docs/11/protocol-flow.html#id-1.10.5.7.9).

## Using SSL/TLS

To configure the client to use SSL connection, you can configure the [`PgConnectOptions`](../../apidocs/io/reactiverse/pgclient/PgConnectOptions.html)
like a Vert.x `NetClient`.
All [SSL modes](https://www.postgresql.org/docs/current/libpq-ssl.html#LIBPQ-SSL-PROTECTION) are supported and you are able to configure `sslmode`. The client is in `DISABLE` SSL mode by default.
`ssl` parameter is kept as a mere shortcut for setting `sslmode`. `setSsl(true)` is equivalent to `setSslMode(VERIFY_CA)` and `setSsl(false)` is equivalent to `setSslMode(DISABLE)`.

```groovy

def options = [
  port:5432,
  host:"the-host",
  database:"the-db",
  user:"user",
  password:"secret",
  sslMode:"VERIFY_CA",
  pemTrustOptions:[
    certPaths:[
      "/path/to/cert.pem"
    ]
  ]
]

PgClient.connect(vertx, options, { res ->
  if (res.succeeded()) {
    // Connected with SSL
  } else {
    println("Could not connect ${res.cause()}")
  }
})

```

More information can be found in the [Vert.x documentation](http://vertx.io/docs/vertx-core/java/#ssl).

## Using a proxy

You can also configure the client to use an HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy.

More information can be found in the [Vert.x documentation](http://vertx.io/docs/vertx-core/java/#_using_a_proxy_for_client_connections).
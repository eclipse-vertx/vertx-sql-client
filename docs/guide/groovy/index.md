# Reactive Postgres Client

The Reactive Postgres Client is a client for Postgres with a straightforward API focusing on
scalability and low overhead.

The client is reactive and non blocking, allowing to handle many database connections with a single thread.

* Event driven
* Lightweight
* Built-in connection pooling
* Prepared queries caching
* Publish / subscribe using Postgres `NOTIFY/LISTEN`
* Batch and cursor support
* Row streaming
* Command pipeling
* RxJava 1 and RxJava 2 support
* Direct memory to object without unnecessary copies
* Java 8 Date and Time support
* SSL/TLS support
* HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy support

## Usage

To use the Reactive Postgres Client add the following dependency to the _dependencies_ section of your build descriptor:

* Maven (in your `pom.xml`):

[source,xml,subs#"+attributes"]
```
<dependency>
 <groupId>io.reactiverse</groupId>
 <artifactId>reactive-pg-client</artifactId>
 <version>0.7.0</version>
</dependency>
```

* Gradle (in your `build.gradle` file):

```groovy
dependencies {
 compile 'io.reactiverse:reactive-pg-client:0.7.0'
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
  username:"user",
  password:"secret",
  maxSize:5
]

// Create the client pool
def client = PgClient.pool(options)

// A simple query
client.query("SELECT * FROM users WHERE id='julien'", { ar ->
  if (ar.succeeded()) {
    def result = ar.result()
    println("Got ${result.size()} results ")
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
  username:"user",
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
  username:"user",
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
  username:"user",
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

## Running queries

When you don't need a transaction or run single queries, you can run queries directly on the pool; the pool
will use one of its connection to run the query and return the result to you.

Here is how to run simple queries:

```groovy
client.query("SELECT * FROM users WHERE id='julien'", { ar ->
  if (ar.succeeded()) {
    def result = ar.result()
    println("Got ${result.size()} results ")
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
    def result = ar.result()
    println("Got ${result.size()} results ")
  } else {
    println("Failure: ${ar.cause().getMessage()}")
  }
})

```

Query methods provides an asynchronous [`PgResult`](../../apidocs/io/reactiverse/pgclient/PgResult.html) instance that works for _SELECT_ queries

```groovy
client.preparedQuery("SELECT first_name, last_name FROM users", { ar ->
  if (ar.succeeded()) {
    def result = ar.result()
    result.each { row ->
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
    def result = ar.result()
    println(result.updatedCount())
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

    // Process results
    def results = res.result()
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
        def result = ar2.result()
      }
    })
  }
})

```

NOTE: prepared query caching depends on the [`setCachePreparedStatements`](../../apidocs/io/reactiverse/pgclient/PgConnectOptions.html#setCachePreparedStatements-boolean-) and
does not depend on whether you are creating prepared queries or use [`direct prepared queries`](../../apidocs/io/reactiverse/pgclient/PgClient.html#preparedQuery-java.lang.String-io.vertx.core.Handler-)

By default prepared query executions fetch all results, you can use a [`PgCursor`](../../apidocs/io/reactiverse/pgclient/PgCursor.html) to control the amount of rows you want to read:

```groovy
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", { ar1 ->
  if (ar1.succeeded()) {
    def pq = ar1.result()

    // Create a cursor
    def cursor = pq.cursor(Tuple.of("julien"))

    // Read 50 rows
    cursor.read(50, { ar2 ->
      if (ar2.succeeded()) {
        def result = ar2.result()

        // Check for more ?
        if (cursor.hasMore()) {

          // Read the next 50
          cursor.read(50, { ar3 ->
            // More results, and so on...
          })
        } else {
          // No more results
        }
      }
    })
  }
})

```

Cursors shall be closed when they are released prematurely:

```groovy
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", { ar1 ->
  if (ar1.succeeded()) {
    def pq = ar1.result()
    def cursor = pq.cursor(Tuple.of("julien"))
    cursor.read(50, { ar2 ->
      if (ar2.succeeded()) {
        // Close the cursor
        cursor.close()
      }
    })
  }
})

```

A stream API is also available for cursors, which can be more convenient, specially with the Rxified version.

```groovy
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1", { ar1 ->
  if (ar1.succeeded()) {
    def pq = ar1.result()

    // Fetch 50 rows at a time
    def stream = pq.createStream(50, Tuple.of("julien"))

    // Use the stream
    stream.exceptionHandler({ err ->
      println("Error: ${err.getMessage()}")
    })
    stream.endHandler({ v ->
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

        // Process results
        def results = res.result()
      } else {
        println("Batch failed ${res.cause()}")
      }
    })
  }
})

```

## Using transactions

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
    })
    conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')", { ar ->
      // Fails and triggers transaction aborts
    })

    // Attempt to commit the transaction
    tx.commit({ ar ->
      // But transaction abortion fails it
    })
  }
})

```

## Postgres type mapping

### Handling JSON

The [`Json`](../../apidocs/io/reactiverse/pgclient/Json.html) Java type is used to represent the Postgres `JSON` and `JSONB` type.

The main reason of this type is handling `null` JSON values.

```groovy

// Create a tuple
def tuple = Tuple.of(Json.create(null), Json.create([
  foo:"bar"
]), Json.create(3))

//
tuple = Tuple.tuple().addJson(Json.create(null)).addJson(Json.create([
  foo:"bar"
])).addJson(Json.create(3))

// JSON object (and arrays) can also be added directly
tuple = Tuple.tuple().addJson(Json.create(null)).addJsonObject([
  foo:"bar"
]).addJson(Json.create(3))

// Retrieving json
def value = tuple.getJson(0).value()

//
value = tuple.getJson(1).value()
value = tuple.getJsonObject(1)

//
value = tuple.getJson(3).value()

```

### Handling NUMERIC

The [`Numeric`](../../apidocs/io/reactiverse/pgclient/Numeric.html) Java type is used to represent the Postgres `NUMERIC` type.

```groovy
def numeric = row.getNumeric("value")
if (numeric.isNaN()) {
  // Handle NaN
} else {
  def value = numeric.bigDecimalValue()
}

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
  username:"user",
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

You can provide a reconnect policy as a function that takes the number of `retries` as argument and returns an `amountOfTime`
value:

* when `amountOfTime < 0`: the subscriber is closed and there is no retry
* when `amountOfTime ## 0`: the subscriber retries to connect immediately
* when `amountOfTime > 0`: the subscriber retries after `amountOfTime` milliseconds

```groovy

def subscriber = PgSubscriber.subscriber(vertx, [
  port:5432,
  host:"the-host",
  database:"the-db",
  username:"user",
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

## Using SSL/TLS

To configure the client to use SSL connection, you can configure the [`PgConnectOptions`](../../apidocs/io/reactiverse/pgclient/PgConnectOptions.html)
like a Vert.x `NetClient`}.

```groovy

def options = [
  port:5432,
  host:"the-host",
  database:"the-db",
  username:"user",
  password:"secret",
  ssl:true,
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

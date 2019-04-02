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

```ruby
require 'reactive-pg-client/pg_client'

# Pool options
options = {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'user' => "user",
  'password' => "secret",
  'maxSize' => 5
}

# Create the client pool
client = ReactivePgClient::PgClient.pool(options)

# A simple query
client.query("SELECT * FROM users WHERE id='julien'") { |ar_err,ar|
  if (ar_err == nil)
    result = ar
    puts "Got #{result.size()} rows "
  else
    puts "Failure: #{ar_err.get_message()}"
  end

  # Now close the pool
  client.close()
}

```

## Connecting to Postgres

Most of the time you will use a pool to connect to Postgres:

```ruby
require 'reactive-pg-client/pg_client'

# Pool options
options = {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'user' => "user",
  'password' => "secret",
  'maxSize' => 5
}

# Create the pooled client
client = ReactivePgClient::PgClient.pool(options)

```

The pooled client uses a connection pool and any operation will borrow a connection from the pool
to execute the operation and release it to the pool.

If you are running with Vert.x you can pass it your Vertx instance:

```ruby
require 'reactive-pg-client/pg_client'

# Pool options
options = {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'user' => "user",
  'password' => "secret",
  'maxSize' => 5
}

# Create the pooled client
client = ReactivePgClient::PgClient.pool(vertx, options)

```

You need to release the pool when you don't need it anymore:

```ruby

# Close the pool and all the associated resources
pool.close()

```

When you need to execute several operations on the same connection, you need to use a client
[`connection`](../../yardoc/ReactivePgClient/PgConnection.html).

You can easily get one from the pool:

```ruby
require 'reactive-pg-client/pg_client'

# Pool options
options = {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'user' => "user",
  'password' => "secret",
  'maxSize' => 5
}

# Create the pooled client
client = ReactivePgClient::PgClient.pool(vertx, options)

# Get a connection from the pool
client.get_connection() { |ar1_err,ar1|

  if (ar1_err == nil)

    puts "Connected"

    # Obtain our connection
    conn = ar1

    # All operations execute on the same connection
    conn.query("SELECT * FROM users WHERE id='julien'") { |ar2_err,ar2|
      if (ar2_err == nil)
        conn.query("SELECT * FROM users WHERE id='emad'") { |ar3_err,ar3|
          # Release the connection to the pool
          conn.close()
        }
      else
        # Release the connection to the pool
        conn.close()
      end
    }
  else
    puts "Could not connect: #{ar1_err.get_message()}"
  end
}

```

Once you are done with the connection you must close it to release it to the pool, so it can be reused.

Sometimes you want to improve performance via Unix domain socket connection, we achieve this with Vert.x Native transports.

Make sure you have added the required `netty-transport-native` dependency in your classpath and enabled the Unix domain socket option.

```ruby
require 'reactive-pg-client/pg_client'

# Pool Options
# Socket file name will be /var/run/postgresql/.s.PGSQL.5432
options = {
  'host' => "/var/run/postgresql",
  'port' => 5432,
  'database' => "the-db"
}

# Create the pooled client
client = ReactivePgClient::PgClient.pool(options)

# Create the pooled client with a vertx instance
# Make sure the vertx instance has enabled native transports
client2 = ReactivePgClient::PgClient.pool(vertx, options)

```

More information can be found in the [Vert.x documentation](https://vertx.io/docs/vertx-core/java/#_native_transports).

## Configuration

There are several options for you to configure the client.

Apart from configuring with a `PgPoolOptions` data object, We also provide you an alternative way to connect when you want to configure with a connection URI:

```ruby
require 'reactive-pg-client/pg_client'

# Connection URI
connectionUri = "postgresql://dbuser:secretpassword@database.server.com:3211/mydb"

# Create the pool from the connection URI
pool = ReactivePgClient::PgClient.pool(connectionUri)

# Create the connection from the connection URI
ReactivePgClient::PgClient.connect(vertx, connectionUri) { |res_err,res|
  # Handling your connection
}

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

```ruby
require 'reactive-pg-client/pg_client'

# Create the pool from the environment variables
pool = ReactivePgClient::PgClient.pool()

# Create the connection from the environment variables
ReactivePgClient::PgClient.connect(vertx) { |res_err,res|
  # Handling your connection
}

```

## Running queries

When you don't need a transaction or run single queries, you can run queries directly on the pool; the pool
will use one of its connection to run the query and return the result to you.

Here is how to run simple queries:

```ruby
client.query("SELECT * FROM users WHERE id='julien'") { |ar_err,ar|
  if (ar_err == nil)
    result = ar
    puts "Got #{result.size()} rows "
  else
    puts "Failure: #{ar_err.get_message()}"
  end
}

```

You can do the same with prepared queries.

The SQL string can refer to parameters by position, using `$1`, `$2`, etc…​

```ruby
require 'reactive-pg-client/tuple'
client.prepared_query("SELECT * FROM users WHERE id=$1", ReactivePgClient::Tuple.of("julien")) { |ar_err,ar|
  if (ar_err == nil)
    rows = ar
    puts "Got #{rows.size()} rows "
  else
    puts "Failure: #{ar_err.get_message()}"
  end
}

```

Query methods provides an asynchronous [`PgRowSet`](../../yardoc/ReactivePgClient/PgRowSet.html) instance that works for _SELECT_ queries

```ruby
client.prepared_query("SELECT first_name, last_name FROM users") { |ar_err,ar|
  if (ar_err == nil)
    rows = ar
    rows.each do |row|
      puts "User #{row.get_string(0)} #{row.get_string(1)}"
    end
  else
    puts "Failure: #{ar_err.get_message()}"
  end
}

```

or _UPDATE_/_INSERT_ queries:

```ruby
require 'reactive-pg-client/tuple'
client.prepared_query("INSERT INTO users (first_name, last_name) VALUES ($1, $2)", ReactivePgClient::Tuple.of("Julien", "Viet")) { |ar_err,ar|
  if (ar_err == nil)
    rows = ar
    puts rows.row_count()
  else
    puts "Failure: #{ar_err.get_message()}"
  end
}

```

The [`Row`](../../yardoc/ReactivePgClient/Row.html) gives you access to your data by index

```ruby
puts "User #{row.get_string(0)} #{row.get_string(1)}"

```

or by name

```ruby
puts "User #{row.get_string("first_name")} #{row.get_string("last_name")}"

```

You can access a wide variety of of types

```ruby

firstName = row.get_string("first_name")
male = row.get_boolean?("male")
age = row.get_integer("age")

# ...


```

You can execute prepared batch

```ruby
require 'reactive-pg-client/tuple'

# Add commands to the batch
batch = Array.new
batch.push(ReactivePgClient::Tuple.of("julien", "Julien Viet"))
batch.push(ReactivePgClient::Tuple.of("emad", "Emad Alblueshi"))

# Execute the prepared batch
client.prepared_batch("INSERT INTO USERS (id, name) VALUES ($1, $2)", batch) { |res_err,res|
  if (res_err == nil)

    # Process rows
    rows = res
  else
    puts "Batch failed #{res_err}"
  end
}

```

You can cache prepared queries:

```ruby
require 'reactive-pg-client/pg_client'

# Enable prepare statements
options['cachePreparedStatements'] = true

client = ReactivePgClient::PgClient.pool(vertx, options)

```

You can fetch generated keys with a 'RETURNING' clause in your query:

```ruby
require 'reactive-pg-client/tuple'
client.prepared_query("INSERT INTO color (color_name) VALUES ($1), ($2), ($3) RETURNING color_id", ReactivePgClient::Tuple.of("white", "red", "blue")) { |ar_err,ar|
  if (ar_err == nil)
    rows = ar
    puts rows.row_count()
    rows.each do |row|
      puts "generated key: #{row.get_integer("color_id")}"
    end
  else
    puts "Failure: #{ar_err.get_message()}"
  end
}

```

## Using connections

When you need to execute sequential queries (without a transaction), you can create a new connection
or borrow one from the pool:

```ruby
Code not translatable
```

Prepared queries can be created:

```ruby
require 'reactive-pg-client/tuple'
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1") { |ar1_err,ar1|
  if (ar1_err == nil)
    pq = ar1
    pq.execute(ReactivePgClient::Tuple.of("julien")) { |ar2_err,ar2|
      if (ar2_err == nil)
        # All rows
        rows = ar2
      end
    }
  end
}

```

NOTE: prepared query caching depends on the [`cachePreparedStatements`](../dataobjects.html#PgConnectOptions#set_cache_prepared_statements-instance_method) and
does not depend on whether you are creating prepared queries or use [`direct prepared queries`](../../yardoc/ReactivePgClient/PgClient.html#prepared_query-instance_method)

[`PgPreparedQuery`](../../yardoc/ReactivePgClient/PgPreparedQuery.html)can perform efficient batching:

```ruby
require 'reactive-pg-client/tuple'
connection.prepare("INSERT INTO USERS (id, name) VALUES ($1, $2)") { |ar1_err,ar1|
  if (ar1_err == nil)
    prepared = ar1

    # Create a query : bind parameters
    batch = Array.new

    # Add commands to the createBatch
    batch.push(ReactivePgClient::Tuple.of("julien", "Julien Viet"))
    batch.push(ReactivePgClient::Tuple.of("emad", "Emad Alblueshi"))

    prepared.batch(batch) { |res_err,res|
      if (res_err == nil)

        # Process rows
        rows = res
      else
        puts "Batch failed #{res_err}"
      end
    }
  end
}

```


## Using transactions

### Transactions with connections

You can execute transaction using SQL `BEGIN`/`COMMIT`/`ROLLBACK`, if you do so you must use
a [`PgConnection`](../../yardoc/ReactivePgClient/PgConnection.html) and manage it yourself.

Or you can use the transaction API of [`PgConnection`](../../yardoc/ReactivePgClient/PgConnection.html):

```ruby
Code not translatable
```

When Postgres reports the current transaction is failed (e.g the infamous _current transaction is aborted, commands ignored until
end of transaction block_), the transaction is rollbacked and the [`abortHandler`](../../yardoc/ReactivePgClient/PgTransaction.html#abort_handler-instance_method)
is called:

```ruby
pool.get_connection() { |res_err,res|
  if (res_err == nil)

    # Transaction must use a connection
    conn = res

    # Begin the transaction
    tx = conn.begin().abort_handler() { |v|
      puts "Transaction failed => rollbacked"
    }

    conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')") { |ar_err,ar|
      # Works fine of course
      if (ar_err == nil)

      else
        tx.rollback()
        conn.close()
      end
    }
    conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')") { |ar_err,ar|
      # Fails and triggers transaction aborts
    }

    # Attempt to commit the transaction
    tx.commit() { |ar_err,ar|
      # But transaction abortion fails it

      # Return the connection to the pool
      conn.close()
    }
  end
}

```

### Simplified transaction API

When you use a pool, you can start a transaction directly on the pool.

It borrows a connection from the pool, begins the transaction and releases the connection to the pool when the transaction ends.

```ruby
Code not translatable
```

## Cursors and streaming

By default prepared query execution fetches all rows, you can use a
[`PgCursor`](../../yardoc/ReactivePgClient/PgCursor.html)to control the amount of rows you want to read:

```ruby
require 'reactive-pg-client/tuple'
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1") { |ar1_err,ar1|
  if (ar1_err == nil)
    pq = ar1

    # Cursors require to run within a transaction
    tx = connection.begin()

    # Create a cursor
    cursor = pq.cursor(ReactivePgClient::Tuple.of("julien"))

    # Read 50 rows
    cursor.read(50) { |ar2_err,ar2|
      if (ar2_err == nil)
        rows = ar2

        # Check for more ?
        if (cursor.has_more?())
          # Repeat the process...
        else
          # No more rows - commit the transaction
          tx.commit()
        end
      end
    }
  end
}

```

PostreSQL destroys cursors at the end of a transaction, so the cursor API shall be used
within a transaction, otherwise you will likely get the `34000` PostgreSQL error.

Cursors shall be closed when they are released prematurely:

```ruby
cursor.read(50) { |ar2_err,ar2|
  if (ar2_err == nil)
    # Close the cursor
    cursor.close()
  end
}

```

A stream API is also available for cursors, which can be more convenient, specially with the Rxified version.

```ruby
require 'reactive-pg-client/tuple'
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1") { |ar1_err,ar1|
  if (ar1_err == nil)
    pq = ar1

    # Streams require to run within a transaction
    tx = connection.begin()

    # Fetch 50 rows at a time
    stream = pq.create_stream(50, ReactivePgClient::Tuple.of("julien"))

    # Use the stream
    stream.exception_handler() { |err|
      puts "Error: #{err.get_message()}"
    }
    stream.end_handler() { |v|
      tx.commit()
      puts "End of stream"
    }
    stream.handler() { |row|
      puts "User: #{row.get_string("last_name")}"
    }
  end
}

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

```ruby
pool.query("SELECT 1::BIGINT \"VAL\"") { |ar_err,ar|
  rowSet = ar
  row = rowSet.iterator().next()

  # Stored as java.lang.Long
  value = row.get_value(0)

  # Convert to java.lang.Integer
  intValue = row.get_integer(0)
}

```

Tuple encoding uses the above type mapping for encoding, unless the type is numeric in which case `java.lang.Number` is used instead:

```ruby
pool.query("SELECT 1::BIGINT \"VAL\"") { |ar_err,ar|
  rowSet = ar
  row = rowSet.iterator().next()

  # Stored as java.lang.Long
  value = row.get_value(0)

  # Convert to java.lang.Integer
  intValue = row.get_integer(0)
}


```

Arrays of these types are supported.

### Handling JSON

The [`Json`](../../yardoc/ReactivePgClient/Json.html) Java type is used to represent the Postgres `JSON` and `JSONB` type.

The main reason of this type is handling `null` JSON values.

```ruby
require 'reactive-pg-client/json'
require 'reactive-pg-client/tuple'

# Create a tuple
tuple = ReactivePgClient::Tuple.of(ReactivePgClient::Json.create(ReactivePgClient::Json.create(nil)), ReactivePgClient::Json.create(ReactivePgClient::Json.create({
  'foo' => "bar"
})), ReactivePgClient::Json.create(ReactivePgClient::Json.create(nil)))

# Retrieving json
value = tuple.get_json(0).value()

#
value = tuple.get_json(1).value()

#
value = tuple.get_json(3).value()

```

### Handling NUMERIC

The [`Numeric`](unavailable) Java type is used to represent the Postgres `NUMERIC` type.

```ruby
numeric = row.get_numeric("value")
if (numeric.na_n?())
  # Handle NaN
else
  value = numeric.big_decimal_value()
end

```

## Handling arrays

Arrays are available on [`Tuple`](../../yardoc/ReactivePgClient/Tuple.html) and [`Row`](../../yardoc/ReactivePgClient/Row.html):

```ruby
Code not translatable
```

## Handling custom types

Strings are used to represent custom types, both sent to and returned from Postgres.

You can read from Postgres and get the custom type as a string

```ruby
require 'reactive-pg-client/tuple'
client.prepared_query("SELECT address, (address).city FROM address_book WHERE id=$1", ReactivePgClient::Tuple.of(3)) { |ar_err,ar|
  if (ar_err == nil)
    rows = ar
    rows.each do |row|
      puts "Full Address #{row.get_string(0)}, City #{row.get_string(1)}"
    end
  else
    puts "Failure: #{ar_err.get_message()}"
  end
}

```

You can also write to Postgres by providing a string

```ruby
require 'reactive-pg-client/tuple'
client.prepared_query("INSERT INTO address_book (id, address) VALUES ($1, $2)", ReactivePgClient::Tuple.of(3, "('Anytown', 'Second Ave', false)")) { |ar_err,ar|
  if (ar_err == nil)
    rows = ar
    puts rows.row_count()
  else
    puts "Failure: #{ar_err.get_message()}"
  end
}

```

## Collector queries

You can use Java collectors with the query API:

```ruby
Code not translatable
```

The collector processing must not keep a reference on the [`Row`](../../yardoc/ReactivePgClient/Row.html) as
there is a single row used for processing the entire set.

The Java `Collectors` provides many interesting predefined collectors, for example you can
create easily create a string directly from the row set:

```ruby
Code not translatable
```

## RxJava support

The rxified API supports RxJava 1 and RxJava 2, the following examples use RxJava 2.

Most asynchronous constructs are available as methods prefixed by `rx`:

```ruby
Code not translatable
```


### Streaming

RxJava 2 supports `Observable` and `Flowable` types, these are exposed using
the [`PgStream`](unavailable) that you can get
from a [`PgPreparedQuery`](unavailable):

```ruby
Code not translatable
```

The same example using `Flowable`:

```ruby
Code not translatable
```

### Transaction

The simplified transaction API allows to easily write transactional
asynchronous flows:

```ruby
Code not translatable
```

## Pub/sub

Postgres supports pub/sub communication channels.

You can set a [`notificationHandler`](../../yardoc/ReactivePgClient/PgConnection.html#notification_handler-instance_method) to receive
Postgres notifications:

```ruby

connection.notification_handler() { |notification|
  puts "Received #{notification['payload']} on channel #{notification['channel']}"
}

connection.query("LISTEN some-channel") { |ar_err,ar|
  puts "Subscribed to channel"
}

```

The [`PgSubscriber`](../../yardoc/ReactivePgClient/PgSubscriber.html) is a channel manager managing a single connection that
provides per channel subscription:

```ruby
require 'reactive-pg-client/pg_subscriber'

subscriber = ReactivePgClient::PgSubscriber.subscriber(vertx, {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'user' => "user",
  'password' => "secret"
})

# You can set the channel before connect
subscriber.channel("channel1").handler() { |payload|
  puts "Received #{payload}"
}

subscriber.connect() { |ar_err,ar|
  if (ar_err == nil)

    # Or you can set the channel after connect
    subscriber.channel("channel2").handler() { |payload|
      puts "Received #{payload}"
    }
  end
}

```

The channel name that is given to the channel method will be the exact name of the channel as held by Postgres for sending
notifications.  Note this is different than the representation of the channel name in SQL, and
internally [`PgSubscriber`](../../yardoc/ReactivePgClient/PgSubscriber.html) will prepare the submitted channel name as a quoted identifier:

```ruby
require 'reactive-pg-client/pg_subscriber'

subscriber = ReactivePgClient::PgSubscriber.subscriber(vertx, {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'user' => "user",
  'password' => "secret"
})

subscriber.connect() { |ar_err,ar|
  if (ar_err == nil)
    # Complex channel name - name in PostgreSQL requires a quoted ID
    subscriber.channel("Complex.Channel.Name").handler() { |payload|
      puts "Received #{payload}"
    }
    subscriber.channel("Complex.Channel.Name").subscribe_handler() { |subscribed|
      subscriber.actual_connection().query("NOTIFY \"Complex.Channel.Name\", 'msg'") { |notified_err,notified|
        puts "Notified \"Complex.Channel.Name\""
      }
    }

    # PostgreSQL simple ID's are forced lower-case
    subscriber.channel("simple_channel").handler() { |payload|
      puts "Received #{payload}"
    }
    subscriber.channel("simple_channel").subscribe_handler() { |subscribed|
      # The following simple channel identifier is forced to lower case
      subscriber.actual_connection().query("NOTIFY Simple_CHANNEL, 'msg'") { |notified_err,notified|
        puts "Notified simple_channel"
      }
    }

    # The following channel name is longer than the current
    # (NAMEDATALEN = 64) - 1 == 63 character limit and will be truncated
    subscriber.channel("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbb").handler() { |payload|
      puts "Received #{payload}"
    }
  end
}

```
You can provide a reconnect policy as a function that takes the number of `retries` as argument and returns an `amountOfTime`
value:

* when `amountOfTime < 0`: the subscriber is closed and there is no retry
* when `amountOfTime = 0`: the subscriber retries to connect immediately
* when `amountOfTime > 0`: the subscriber retries after `amountOfTime` milliseconds

```ruby
require 'reactive-pg-client/pg_subscriber'

subscriber = ReactivePgClient::PgSubscriber.subscriber(vertx, {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'user' => "user",
  'password' => "secret"
})

# Reconnect at most 10 times after 100 ms each
subscriber.reconnect_policy(lambda { |retries|
  if (retries < 10)
    return 100
  else
    return -1
  end
})

```

The default policy is to not reconnect.

## Cancelling Request

Postgres supports cancellation of requests in progress. You can cancel inflight requests using [`cancelRequest`](../../yardoc/ReactivePgClient/PgConnection.html#cancel_request-instance_method). Cancelling a request opens a new connection to the server and cancels the request and then close the connection.

```ruby
connection.query("SELECT pg_sleep(20)") { |ar_err,ar|
  if (ar_err == nil)
    # imagine this is a long query and is still running
    puts "Query success"
  else
    # the server will abort the current query after cancelling request
    puts "Failed to query due to #{ar_err.get_message()}"
  end
}
connection.cancel_request() { |ar_err,ar|
  if (ar_err == nil)
    puts "Cancelling request has been sent"
  else
    puts "Failed to send cancelling request"
  end
}

```

> The cancellation signal might or might not have any effect — for example, if it arrives after the backend has finished processing the query, then it will have no effect. If the cancellation is effective, it results in the current command being terminated early with an error message.

More information can be found in the [official documentation](https://www.postgresql.org/docs/11/protocol-flow.html#id-1.10.5.7.9).

## Using SSL/TLS

To configure the client to use SSL connection, you can configure the [`PgConnectOptions`](../dataobjects.html#PgConnectOptions)
like a Vert.x `NetClient`.
All [SSL modes](https://www.postgresql.org/docs/current/libpq-ssl.html#LIBPQ-SSL-PROTECTION) are supported and you are able to configure `sslmode`. The client is in `DISABLE` SSL mode by default.
`ssl` parameter is kept as a mere shortcut for setting `sslmode`. `setSsl(true)` is equivalent to `setSslMode(VERIFY_CA)` and `setSsl(false)` is equivalent to `setSslMode(DISABLE)`.

```ruby
require 'reactive-pg-client/pg_client'

options = {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'user' => "user",
  'password' => "secret",
  'sslMode' => "VERIFY_CA",
  'pemTrustOptions' => {
    'certPaths' => [
      "/path/to/cert.pem"
    ]
  }
}

ReactivePgClient::PgClient.connect(vertx, options) { |res_err,res|
  if (res_err == nil)
    # Connected with SSL
  else
    puts "Could not connect #{res_err}"
  end
}

```

More information can be found in the [Vert.x documentation](http://vertx.io/docs/vertx-core/java/#ssl).

## Using a proxy

You can also configure the client to use an HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy.

More information can be found in the [Vert.x documentation](http://vertx.io/docs/vertx-core/java/#_using_a_proxy_for_client_connections).
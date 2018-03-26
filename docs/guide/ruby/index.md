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

```ruby
require 'reactive-pg-client/pg_client'

# Pool options
options = {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'username' => "user",
  'password' => "secret",
  'maxSize' => 5
}

# Create the client pool
client = ReactivePgClient::PgClient.pool(options)

# A simple query
client.query("SELECT * FROM users WHERE id='julien'") { |ar_err,ar|
  if (ar_err == nil)
    result = ar
    puts "Got #{result.size()} results "
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
  'username' => "user",
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
  'username' => "user",
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
  'username' => "user",
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

## Running queries

When you don't need a transaction or run single queries, you can run queries directly on the pool; the pool
will use one of its connection to run the query and return the result to you.

Here is how to run simple queries:

```ruby
client.query("SELECT * FROM users WHERE id='julien'") { |ar_err,ar|
  if (ar_err == nil)
    result = ar
    puts "Got #{result.size()} results "
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
    result = ar
    puts "Got #{result.size()} results "
  else
    puts "Failure: #{ar_err.get_message()}"
  end
}

```

Query methods provides an asynchronous [`PgResult`](../../yardoc/ReactivePgClient/PgResult.html) instance that works for _SELECT_ queries

```ruby
client.prepared_query("SELECT first_name, last_name FROM users") { |ar_err,ar|
  if (ar_err == nil)
    result = ar
    result.each do |row|
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
    result = ar
    puts result.updated_count()
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

    # Process results
    results = res
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
        result = ar2
      end
    }
  end
}

```

NOTE: prepared query caching depends on the [`cachePreparedStatements`](../dataobjects.html#PgConnectOptions#set_cache_prepared_statements-instance_method) and
does not depend on whether you are creating prepared queries or use [`direct prepared queries`](../../yardoc/ReactivePgClient/PgClient.html#prepared_query-instance_method)

By default prepared query executions fetch all results, you can use a [`PgCursor`](../../yardoc/ReactivePgClient/PgCursor.html) to control the amount of rows you want to read:

```ruby
require 'reactive-pg-client/tuple'
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1") { |ar1_err,ar1|
  if (ar1_err == nil)
    pq = ar1

    # Create a cursor
    cursor = pq.cursor(ReactivePgClient::Tuple.of("julien"))

    # Read 50 rows
    cursor.read(50) { |ar2_err,ar2|
      if (ar2_err == nil)
        result = ar2

        # Check for more ?
        if (cursor.has_more?())

          # Read the next 50
          cursor.read(50) { |ar3_err,ar3|
            # More results, and so on...
          }
        else
          # No more results
        end
      end
    }
  end
}

```

Cursors shall be closed when they are released prematurely:

```ruby
require 'reactive-pg-client/tuple'
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1") { |ar1_err,ar1|
  if (ar1_err == nil)
    pq = ar1
    cursor = pq.cursor(ReactivePgClient::Tuple.of("julien"))
    cursor.read(50) { |ar2_err,ar2|
      if (ar2_err == nil)
        # Close the cursor
        cursor.close()
      end
    }
  end
}

```

A stream API is also available for cursors, which can be more convenient, specially with the Rxified version.

```ruby
require 'reactive-pg-client/tuple'
connection.prepare("SELECT * FROM users WHERE first_name LIKE $1") { |ar1_err,ar1|
  if (ar1_err == nil)
    pq = ar1

    # Fetch 50 rows at a time
    stream = pq.create_stream(50, ReactivePgClient::Tuple.of("julien"))

    # Use the stream
    stream.exception_handler() { |err|
      puts "Error: #{err.get_message()}"
    }
    stream.end_handler() { |v|
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

        # Process results
        results = res
      else
        puts "Batch failed #{res_err}"
      end
    }
  end
}

```

## Using transactions

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
    }
    conn.query("INSERT INTO Users (first_name,last_name) VALUES ('Julien','Viet')") { |ar_err,ar|
      # Fails and triggers transaction aborts
    }

    # Attempt to commit the transaction
    tx.commit() { |ar_err,ar|
      # But transaction abortion fails it
    }
  end
}

```

## Postgres type mapping

### Handling JSON

The [`Json`](../../yardoc/ReactivePgClient/Json.html) Java type is used to represent the Postgres `JSON` and `JSONB` type.

The main reason of this type is handling `null` JSON values.

```ruby
require 'reactive-pg-client/json'
require 'reactive-pg-client/tuple'

# Create a tuple
tuple = ReactivePgClient::Tuple.of(ReactivePgClient::Json.create(nil), ReactivePgClient::Json.create({
  'foo' => "bar"
}), ReactivePgClient::Json.create(3))

#
tuple = ReactivePgClient::Tuple.tuple().add_json(ReactivePgClient::Json.create(nil)).add_json(ReactivePgClient::Json.create({
  'foo' => "bar"
})).add_json(ReactivePgClient::Json.create(3))

# JSON object (and arrays) can also be added directly
tuple = ReactivePgClient::Tuple.tuple().add_json(ReactivePgClient::Json.create(nil)).add_json_object({
  'foo' => "bar"
}).add_json(ReactivePgClient::Json.create(3))

# Retrieving json
value = tuple.get_json(0).value()

#
value = tuple.get_json(1).value()
value = tuple.get_json_object(1)

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
  'username' => "user",
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

You can provide a reconnect policy as a function that takes the number of `retries` as argument and returns an `amountOfTime`
value:

* when `amountOfTime < 0`: the subscriber is closed and there is no retry
* when `amountOfTime ## 0`: the subscriber retries to connect immediately
* when `amountOfTime > 0`: the subscriber retries after `amountOfTime` milliseconds

```ruby
require 'reactive-pg-client/pg_subscriber'

subscriber = ReactivePgClient::PgSubscriber.subscriber(vertx, {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'username' => "user",
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

## Using SSL/TLS

To configure the client to use SSL connection, you can configure the [`PgConnectOptions`](../dataobjects.html#PgConnectOptions)
like a Vert.x `NetClient`}.

```ruby
require 'reactive-pg-client/pg_client'

options = {
  'port' => 5432,
  'host' => "the-host",
  'database' => "the-db",
  'username' => "user",
  'password' => "secret",
  'ssl' => true,
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

More information can be found in the http://vertx.io/docs/vertx-core/java/#_using_a_proxy_for_client_connections[Vert.x documentation].
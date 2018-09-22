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
  <groupId>${maven.groupId}</groupId>
  <artifactId>${maven.artifactId}</artifactId>
  <version>${maven.version}</version>
</dependency>
```

* Gradle (in your `build.gradle` file):

```groovy
dependencies {
  compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
}
```

## Getting started

Here is the simplest way to connect, query and disconnect

```$lang
{@link examples.Examples#gettingStarted()}
```

## Connecting to Postgres

Most of the time you will use a pool to connect to Postgres:

```$lang
{@link examples.Examples#connecting01}
```

The pooled client uses a connection pool and any operation will borrow a connection from the pool
to execute the operation and release it to the pool.

If you are running with Vert.x you can pass it your Vertx instance:

```$lang
{@link examples.Examples#connecting02}
```

You need to release the pool when you don't need it anymore:

```$lang
{@link examples.Examples#connecting03}
```

When you need to execute several operations on the same connection, you need to use a client
{@link io.reactiverse.pgclient.PgConnection connection}.

You can easily get one from the pool:

```$lang
{@link examples.Examples#connecting04}
```

Once you are done with the connection you must close it to release it to the pool, so it can be reused.

Sometimes you want to improve performance via Unix domain socket connection, we achieve this with Vert.x Native transports.

Make sure you have added the required `netty-transport-native` dependency in your classpath and enabled the Unix domain socket option.

```$lang
{@link examples.Examples#connecting06}
```

More information can be found in the [Vert.x documentation](https://vertx.io/docs/vertx-core/java/#_native_transports).

## Configuration

There are several options for you to configure the client.

Apart from configuring with a `PgPoolOptions` data object, We also provide you an alternative way to connect when you want to configure with a connection URI:

```$lang
{@link examples.Examples#configureFromUri(io.vertx.core.Vertx)}
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

```$lang
{@link examples.Examples#configureFromEnv(io.vertx.core.Vertx)}
```

## Running queries

When you don't need a transaction or run single queries, you can run queries directly on the pool; the pool
will use one of its connection to run the query and return the result to you.

Here is how to run simple queries:

```$lang
{@link examples.Examples#queries01(io.reactiverse.pgclient.PgClient)}
```

You can do the same with prepared queries.

The SQL string can refer to parameters by position, using `$1`, `$2`, etc…​

```$lang
{@link examples.Examples#queries02(io.reactiverse.pgclient.PgClient)}
```

Query methods provides an asynchronous {@link io.reactiverse.pgclient.PgRowSet} instance that works for _SELECT_ queries

```$lang
{@link examples.Examples#queries03(io.reactiverse.pgclient.PgClient)}
```

or _UPDATE_/_INSERT_ queries:

```$lang
{@link examples.Examples#queries04(io.reactiverse.pgclient.PgClient)}
```

The {@link io.reactiverse.pgclient.Row} gives you access to your data by index

```$lang
{@link examples.Examples#queries05(Row)}
```

or by name

```$lang
{@link examples.Examples#queries06(Row)}
```

You can access a wide variety of of types

```$lang
{@link examples.Examples#queries07(Row)}
```

You can execute prepared batch

```$lang
{@link examples.Examples#queries08(io.reactiverse.pgclient.PgClient)}
```

You can cache prepared queries:

```$lang
{@link examples.Examples#queries09(io.vertx.core.Vertx, PgPoolOptions)}
```

## Using connections

When you need to execute sequential queries (without a transaction), you can create a new connection
or borrow one from the pool:

```$lang
{@link examples.Examples#usingConnections01(io.vertx.core.Vertx, io.reactiverse.pgclient.PgPool)}
```

Prepared queries can be created:

```$lang
{@link examples.Examples#usingConnections02(io.reactiverse.pgclient.PgConnection)}
```

NOTE: prepared query caching depends on the {@link io.reactiverse.pgclient.PgConnectOptions#setCachePreparedStatements(boolean)} and
does not depend on whether you are creating prepared queries or use {@link io.reactiverse.pgclient.PgClient#preparedQuery(java.lang.String, io.vertx.core.Handler) direct prepared queries}

By default prepared query executions fetch all rows, you can use a {@link io.reactiverse.pgclient.PgCursor} to control the amount of rows you want to read:

```$lang
{@link examples.Examples#usingConnections03(io.reactiverse.pgclient.PgConnection)}
```

Cursors shall be closed when they are released prematurely:

```$lang
{@link examples.Examples#usingConnections04(io.reactiverse.pgclient.PgConnection)}
```

A stream API is also available for cursors, which can be more convenient, specially with the Rxified version.

```$lang
{@link examples.Examples#usingConnections05(io.reactiverse.pgclient.PgConnection)}
```

The stream read the rows by batch of `50` and stream them, when the rows have been passed to the handler,
a new batch of `50` is read and so on.

The stream can be resumed or paused, the loaded rows will remain in memory until they are delivered and the cursor
will stop iterating.

{@link io.reactiverse.pgclient.PgPreparedQuery} can perform efficient batching:

```$lang
{@link examples.Examples#usingConnections06(io.reactiverse.pgclient.PgConnection)}
```

## Using transactions

### Transactions with connections

You can execute transaction using SQL `BEGIN`/`COMMIT`/`ROLLBACK`, if you do so you must use
a {@link io.reactiverse.pgclient.PgConnection} and manage it yourself.

Or you can use the transaction API of {@link io.reactiverse.pgclient.PgConnection}:

```$lang
{@link examples.Examples#transaction01(io.reactiverse.pgclient.PgPool)}
```

When Postgres reports the current transaction is failed (e.g the infamous _current transaction is aborted, commands ignored until
end of transaction block_), the transaction is rollbacked and the {@link io.reactiverse.pgclient.PgTransaction#abortHandler(io.vertx.core.Handler)}
is called:

```$lang
{@link examples.Examples#transaction02(io.reactiverse.pgclient.PgPool)}
```

### Simplified transaction API

When you use a pool, you can start a transaction directly on the pool.

It borrows a connection from the pool, begins the transaction and releases the connection to the pool when the transaction ends.

```$lang
{@link examples.Examples#transaction03(io.reactiverse.pgclient.PgPool)}
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

```$lang
{@link examples.Examples#typeMapping01}
```

Tuple encoding uses the above type mapping for encoding, unless the type is numeric in which case `java.lang.Number` is used instead:

```$lang
{@link examples.Examples#typeMapping02}
```

Arrays of these types are supported.

### Handling JSON

The {@link io.reactiverse.pgclient.data.Json} Java type is used to represent the Postgres `JSON` and `JSONB` type.

The main reason of this type is handling `null` JSON values.

```$lang
{@link examples.Examples#jsonExample()}
```

### Handling NUMERIC

The {@link io.reactiverse.pgclient.data.Numeric} Java type is used to represent the Postgres `NUMERIC` type.

```$lang
{@link examples.Examples#numericExample}
```

## Handling arrays

Arrays are available on {@link io.reactiverse.pgclient.Tuple} and {@link io.reactiverse.pgclient.Row}:

```$lang
{@link examples.Examples#arrayExample}
```

## Handling custom types

Strings are used to represent custom types, both sent to and returned from Postgres.

You can read from Postgres and get the custom type as a string

```$lang
{@link examples.Examples#customType01Example}
```

You can also write to Postgres by providing a string

```$lang
{@link examples.Examples#customType02Example}
```

## Collector queries

You can use Java collectors with the query API:

```$lang
{@link examples.Examples#collector01Example}
```

The collector processing must not keep a reference on the {@link io.reactiverse.pgclient.Row} as
there is a single row used for processing the entire set.

The Java `Collectors` provides many interesting predefined collectors, for example you can
create easily create a string directly from the row set:

```$lang
{@link examples.Examples#collector02Example}
```

## RxJava support

The rxified API supports RxJava 1 and RxJava 2, the following examples use RxJava 2.

Most asynchronous constructs are available as methods prefixed by `rx`:

```$lang
{@link examples.RxExamples#simpleQuery01Example}
```


### Streaming

RxJava 2 supports `Observable` and `Flowable` types, these are exposed using
the {@link io.reactiverse.reactivex.pgclient.PgStream} that you can get
from a {@link io.reactiverse.reactivex.pgclient.PgPreparedQuery}:

```$lang
{@link examples.RxExamples#streamingQuery01Example}
```

The same example using `Flowable`:

```$lang
{@link examples.RxExamples#streamingQuery02Example}
```

### Transaction

The simplified transaction API allows to easily write transactional
asynchronous flows:

```$lang
{@link examples.RxExamples#transaction01Example}
```

## Pub/sub

Postgres supports pub/sub communication channels.

You can set a {@link io.reactiverse.pgclient.PgConnection#notificationHandler(io.vertx.core.Handler)} to receive
Postgres notifications:

```$lang
{@link examples.Examples#pubsub01(io.reactiverse.pgclient.PgConnection)}
```

The {@link io.reactiverse.pgclient.pubsub.PgSubscriber} is a channel manager managing a single connection that
provides per channel subscription:

```$lang
{@link examples.Examples#pubsub02(io.vertx.core.Vertx)}
```

The channel name that is given to the channel method will be the exact name of the channel as held by Postgres for sending
notifications.  Note this is different than the representation of the channel name in SQL, and
internally {@link io.reactiverse.pgclient.pubsub.PgSubscriber} will prepare the submitted channel name as a quoted identifier:

```$lang
{@link examples.Examples#pubsub03(io.vertx.core.Vertx)}
```
You can provide a reconnect policy as a function that takes the number of `retries` as argument and returns an `amountOfTime`
value:

* when `amountOfTime < 0`: the subscriber is closed and there is no retry
* when `amountOfTime = 0`: the subscriber retries to connect immediately
* when `amountOfTime > 0`: the subscriber retries after `amountOfTime` milliseconds

```$lang
{@link examples.Examples#pubsub04(io.vertx.core.Vertx)}
```

The default policy is to not reconnect.

## Using SSL/TLS

To configure the client to use SSL connection, you can configure the {@link io.reactiverse.pgclient.PgConnectOptions}
like a Vert.x `NetClient`.

```$lang
{@link examples.Examples#ex10}
```

More information can be found in the [Vert.x documentation](http://vertx.io/docs/vertx-core/java/#ssl).

## Using a proxy

You can also configure the client to use an HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy.

More information can be found in the [Vert.x documentation](http://vertx.io/docs/vertx-core/java/#_using_a_proxy_for_client_connections).

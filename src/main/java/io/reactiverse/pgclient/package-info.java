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

/**
 * = Reactive Postgres Client
 *
 * The Reactive Postgres Client is a client for Postgres with a straightforward API focusing on
 * scalability and low overhead.
 *
 * The client is reactive and non blocking, allowing to handle many database connections with a single thread.
 *
 * * Event driven
 * * Lightweight
 * * Built-in connection pooling
 * * Prepared queries caching
 * * Publish / subscribe using Postgres `NOTIFY/LISTEN`
 * * Batch and cursor support
 * * Row streaming
 * * Command pipeling
 * * RxJava 1 and RxJava 2 support
 * * Direct memory to object without unnecessary copies
 * * Java 8 Date and Time support
 * * SSL/TLS support
 * * HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy support
 *
 * == Usage
 *
 * To use the Reactive Postgres Client add the following dependency to the _dependencies_ section of your build descriptor:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 *   <groupId>${maven.groupId}</groupId>
 *   <artifactId>${maven.artifactId}</artifactId>
 *   <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * dependencies {
 *   compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * }
 * ----
 *
 * == Getting started
 *
 * Here is the simplest way to connect, query and disconnect
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#gettingStarted()}
 * ----
 *
 * You can also use environment variables to set default connection setting values, this is useful
 * when you want to avoid hard-coding database connection information.You can refer to the https://www.postgresql.org/docs/9.6/static/libpq-connect.html#LIBPQ-CONNSTRING[official documentation] for more details.
 * The following parameters are supported:
 *
 * * `PGHOST`
 * * `PGHOSTADDR`
 * * `PGPORT`
 * * `PGDATABASE`
 * * `PGUSER`
 * * `PGPASSWORD`
 *
 * We also provide you an alternative when you want to configure with a connection uri:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#connectingWithUri()}
 * ----
 *
 * More information about connection string formats can be found in the https://www.postgresql.org/docs/9.6/static/libpq-connect.html#LIBPQ-CONNSTRING[PostgreSQL Manuals].
 *
 * == Connecting to Postgres
 *
 * Most of the time you will use a pool to connect to Postgres:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#connecting01}
 * ----
 *
 * The pooled client uses a connection pool and any operation will borrow a connection from the pool
 * to execute the operation and release it to the pool.
 *
 * If you are running with Vert.x you can pass it your Vertx instance:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#connecting02}
 * ----
 *
 * You need to release the pool when you don't need it anymore:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#connecting03}
 * ----
 *
 * When you need to execute several operations on the same connection, you need to use a client
 * {@link io.reactiverse.pgclient.PgConnection connection}.
 *
 * You can easily get one from the pool:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#connecting04}
 * ----
 *
 * Once you are done with the connection you must close it to release it to the pool, so it can be reused.
 *
 * == Running queries
 *
 * When you don't need a transaction or run single queries, you can run queries directly on the pool; the pool
 * will use one of its connection to run the query and return the result to you.
 *
 * Here is how to run simple queries:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries01(io.reactiverse.pgclient.PgClient)}
 * ----
 *
 * You can do the same with prepared queries.
 *
 * The SQL string can refer to parameters by position, using `$1`, `$2`, etc…​
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries02(io.reactiverse.pgclient.PgClient)}
 * ----
 *
 * Query methods provides an asynchronous {@link io.reactiverse.pgclient.PgResult} instance that works for _SELECT_ queries
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries03(io.reactiverse.pgclient.PgClient)}
 * ----
 *
 * or _UPDATE_/_INSERT_ queries:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries04(io.reactiverse.pgclient.PgClient)}
 * ----
 *
 * The {@link io.reactiverse.pgclient.Row} gives you access to your data by index
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries05(Row)}
 * ----
 *
 * or by name
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries06(Row)}
 * ----
 *
 * You can access a wide variety of of types
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries07(Row)}
 * ----
 *
 * You can execute prepared batch
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries08(io.reactiverse.pgclient.PgClient)}
 * ----
 *
 * You can cache prepared queries:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries09(io.vertx.core.Vertx, PgPoolOptions)}
 * ----
 *
 * == Using connections
 *
 * When you need to execute sequential queries (without a transaction), you can create a new connection
 * or borrow one from the pool:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections01(io.vertx.core.Vertx, io.reactiverse.pgclient.PgPool)}
 * ----
 *
 * Prepared queries can be created:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections02(io.reactiverse.pgclient.PgConnection)}
 * ----
 *
 * NOTE: prepared query caching depends on the {@link io.reactiverse.pgclient.PgConnectOptions#setCachePreparedStatements(boolean)} and
 * does not depend on whether you are creating prepared queries or use {@link io.reactiverse.pgclient.PgClient#preparedQuery(java.lang.String, io.vertx.core.Handler) direct prepared queries}
 *
 * By default prepared query executions fetch all results, you can use a {@link io.reactiverse.pgclient.PgCursor} to control the amount of rows you want to read:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections03(io.reactiverse.pgclient.PgConnection)}
 * ----
 *
 * Cursors shall be closed when they are released prematurely:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections04(io.reactiverse.pgclient.PgConnection)}
 * ----
 *
 * A stream API is also available for cursors, which can be more convenient, specially with the Rxified version.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections05(io.reactiverse.pgclient.PgConnection)}
 * ----
 *
 * The stream read the rows by batch of `50` and stream them, when the rows have been passed to the handler,
 * a new batch of `50` is read and so on.
 *
 * The stream can be resumed or paused, the loaded rows will remain in memory until they are delivered and the cursor
 * will stop iterating.
 *
 * {@link io.reactiverse.pgclient.PgPreparedQuery} can perform efficient batching:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections06(io.reactiverse.pgclient.PgConnection)}
 * ----
 *
 * == Using transactions
 *
 * You can execute transaction using SQL `BEGIN`/`COMMIT`/`ROLLBACK`, if you do so you must use
 * a {@link io.reactiverse.pgclient.PgConnection} and manage it yourself.
 *
 * Or you can use the transaction API of {@link io.reactiverse.pgclient.PgConnection}:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#transaction01(io.reactiverse.pgclient.PgPool)}
 * ----
 *
 * When Postgres reports the current transaction is failed (e.g the infamous _current transaction is aborted, commands ignored until
 * end of transaction block_), the transaction is rollbacked and the {@link io.reactiverse.pgclient.PgTransaction#abortHandler(io.vertx.core.Handler)}
 * is called:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#transaction02(io.reactiverse.pgclient.PgPool)}
 * ----
 *
 * == Postgres type mapping
 *
 * === Handling JSON
 *
 * The {@link io.reactiverse.pgclient.Json} Java type is used to represent the Postgres `JSON` and `JSONB` type.
 *
 * The main reason of this type is handling {@code null} JSON values.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#jsonExample()}
 * ----
 *
 * === Handling NUMERIC
 *
 * The {@link io.reactiverse.pgclient.Numeric} Java type is used to represent the Postgres `NUMERIC` type.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#numericExample(Row)}
 * ----
 *
 * === Handling arrays
 *
 * You can map java arrays to Postgres arrays.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#stringArrayExample(io.reactiverse.pgclient.Row)}
 * ----
 *
 * Primitive arrays are used whenever possible.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#intArrayExample(io.reactiverse.pgclient.Row)}
 * ----
 *
 * == Pub/sub
 *
 * Postgres supports pub/sub communication channels.
 *
 * You can set a {@link io.reactiverse.pgclient.PgConnection#notificationHandler(io.vertx.core.Handler)} to receive
 * Postgres notifications:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#pubsub01(io.reactiverse.pgclient.PgConnection)}
 * ----
 *
 * The {@link io.reactiverse.pgclient.pubsub.PgSubscriber} is a channel manager managing a single connection that
 * provides per channel subscription:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#pubsub02(io.vertx.core.Vertx)}
 * ----
 *
 * You can provide a reconnect policy as a function that takes the number of `retries` as argument and returns an `amountOfTime`
 * value:
 *
 * * when `amountOfTime < 0`: the subscriber is closed and there is no retry
 * * when `amountOfTime == 0`: the subscriber retries to connect immediately
 * * when `amountOfTime > 0`: the subscriber retries after `amountOfTime` milliseconds
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#pubsub03(io.vertx.core.Vertx)}
 * ----
 *
 * The default policy is to not reconnect.
 *
 * == Using SSL/TLS
 *
 * To configure the client to use SSL connection, you can configure the {@link io.reactiverse.pgclient.PgConnectOptions}
 * like a Vert.x {@code NetClient}.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex10}
 * ----
 *
 * More information can be found in the http://vertx.io/docs/vertx-core/java/#ssl[Vert.x documentation].
 *
 * == Using a proxy
 *
 * You can also configure the client to use an HTTP/1.x CONNECT, SOCKS4a or SOCKS5 proxy.
 *
 * More information can be found in the http://vertx.io/docs/vertx-core/java/#_using_a_proxy_for_client_connections[Vert.x documentation].
 *
 */
@ModuleGen(name = "reactive-pg-client", groupPackage = "io.reactiverse")
package io.reactiverse.pgclient;

import io.vertx.codegen.annotations.ModuleGen;

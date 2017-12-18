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
 * == Connecting to Postgres
 *
 * Most of the time you will use a pool to connect to Postgres:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#connecting01}
 * ----
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
 * You can also connect directly to Postgres without a pool
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#connecting04}
 * ----
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
 * {@link examples.Examples#queries01(com.julienviet.pgclient.PgClient)}
 * ----
 *
 * You can do the same with prepared queries.
 *
 * The sql string can refer to parameters by position, using `$1`, `$2`, etc…​
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries02(com.julienviet.pgclient.PgClient)}
 * ----
 *
 * Query methods return a {@link com.julienviet.pgclient.PgResult} instance that works for _select_ statements
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries03(com.julienviet.pgclient.PgClient)}
 * ----
 *
 * or _update_/_insert_ statements:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries04(com.julienviet.pgclient.PgClient)}
 * ----
 *
 * The {@link com.julienviet.pgclient.Row} gives you access to your data by index
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries05(com.julienviet.pgclient.Row)}
 * ----
 *
 * or by name
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries06(com.julienviet.pgclient.Row)}
 * ----
 *
 * You can access a wide variety of of types
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries07(com.julienviet.pgclient.Row)}
 * ----
 *
 * You can execute prepared batch
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries08(com.julienviet.pgclient.PgClient)}
 * ----
 *
 * You can cache prepared statements:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#queries09(io.vertx.core.Vertx, com.julienviet.pgclient.PgPoolOptions)}
 * ----
 *
 * == Using connections
 *
 * When you need to execute sequential queries (without a transaction), you can create a new connection
 * or borrow one from the pool:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections01(io.vertx.core.Vertx, com.julienviet.pgclient.PgPool)}
 * ----
 *
 * Prepared queries can be managed:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections02(com.julienviet.pgclient.PgConnection)}
 * ----
 *
 * NOTE: prepared query caching depends on the {@link com.julienviet.pgclient.PgConnectOptions#setCachePreparedStatements(boolean)} and
 * does not depend on whether you are creating prepared statement or use {@link com.julienviet.pgclient.PgClient#preparedQuery(java.lang.String, io.vertx.core.Handler) direct prepared queries}
 *
 * By default the query will fetch all results, you can override this and define a maximum fetch size using cursors:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections03(com.julienviet.pgclient.PgConnection)}
 * ----
 *
 * Cursors shall be closed when they are released prematurely:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections04(com.julienviet.pgclient.PgConnection)}
 * ----
 *
 * A stream API is also available for cursors:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections05(com.julienviet.pgclient.PgConnection)}
 * ----
 *
 * The stream fetches the rows by batch of `50` and stream them, when the rows have been passed to the handler,
 * a new batch of `50` is loaded and so on.
 *
 * The stream can be resumed or paused, the loaded rows will remain in memory until they are delivered and the cursor
 * will stop iterating.
 *
 * You can also use {@link com.julienviet.pgclient.PgPreparedStatement} for efficient batching:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#usingConnections06(com.julienviet.pgclient.PgConnection)}
 * ----
 *
 * == Using transactions
 *
 * You can execute transaction using SQL `BEGIN`/`COMMIT`/`ROLLBACK`, if you do so you must use
 * a {@link com.julienviet.pgclient.PgConnection} and manage it yourself.
 *
 * Or you can use the transaction API of {@link com.julienviet.pgclient.PgConnection}:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#transaction01(com.julienviet.pgclient.PgPool)}
 * ----
 *
 * When Postgres reports the current transaction is failed (e.g the infamous _current transaction is aborted, commands ignored until
 * end of transaction block_), the transaction is rollbacked and the {@link com.julienviet.pgclient.PgTransaction#abortHandler(io.vertx.core.Handler)}
 * is called:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#transaction02(com.julienviet.pgclient.PgPool)}
 * ----
 *
 * == Using SSL/TLS
 *
 * To configure the client to use SSL connection, you can configure the {@link com.julienviet.pgclient.PgConnectOptions}
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
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-pg-client", groupPackage = "com.julienviet")
package com.julienviet.pgclient;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;

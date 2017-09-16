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
 * = Postgres Client for Eclipse Vert.x
 *
 * == Using the client
 *
 * To use Postgres Client for Vert.x add the following dependency to the _dependencies_ section of your build descriptor:
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
 * == Connecting to a database
 *
 * You can use the client to connect to the database and interact with it.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex1}
 * ----
 *
 * You can create a pool of connection to obtain a connection instead:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex2}
 * ----
 *
 * When you are done with the pool, you should close it:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex3}
 * ----
 *
 * == Prepared statements
 *
 * Prepared statements can be created and managed by the application.
 *
 * The `sql` string can refer to parameters by position, using $1, $2, etc...
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex4}
 * ----
 *
 * When you are done with the prepared statement, you should close it:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex5}
 * ----
 *
 * NOTE: when you close the connection, you don't need to close its prepared statements
 *
 * By default the query will fetch all results, you can override this and define a maximum fetch size.
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex6}
 * ----
 *
 * When a query is not completed you can call {@link com.julienviet.pgclient.PgQuery#close()} to release
 * the query result in progress:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex7}
 * ----
 *
 * Prepared statements can also be used for update operations
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex8}
 * ----
 *
 *
 * Prepared statements can also be used to batch operations in a very efficient manner:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex9}
 * ----
 *
 * == Using SSL/TLS
 *
 * To configure the client to use SSL connection, you can configure the {@link com.julienviet.pgclient.PgClient}
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

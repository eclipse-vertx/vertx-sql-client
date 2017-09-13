/*
 * Copyright 2015 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 *
 * Copyright (c) 2015 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
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
 * Prepared statements can also be used to batch operations in a very efficient manner:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#ex8}
 * ----
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-pg-client", groupPackage = "com.julienviet")
package com.julienviet.pgclient;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;

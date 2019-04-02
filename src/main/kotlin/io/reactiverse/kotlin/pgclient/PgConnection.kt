/*
 * Copyright 2019 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgConnection
import io.reactiverse.pgclient.PgPreparedQuery
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Suspending version of method [io.reactiverse.pgclient.PgConnection.prepare]
 *
 * @param sql the sql
 * @return [PgPreparedQuery]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgConnection] using Vert.x codegen.
 */
suspend fun PgConnection.prepareAwait(sql: String): PgPreparedQuery {
  return awaitResult {
    this.prepare(sql, it)
  }
}

suspend fun PgConnection.preparedQueryAwait(sql: String): PgRowSet {
  return awaitResult {
    this.preparedQuery(sql, it)
  }
}

suspend fun PgConnection.queryAwait(sql: String): PgRowSet {
  return awaitResult {
    this.query(sql, it)
  }
}

suspend fun PgConnection.preparedQueryAwait(sql: String, arguments: Tuple): PgRowSet {
  return awaitResult {
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun PgConnection.preparedBatchAwait(sql: String, batch: List<Tuple>): PgRowSet {
  return awaitResult {
    this.preparedBatch(sql, batch, it)
  }
}


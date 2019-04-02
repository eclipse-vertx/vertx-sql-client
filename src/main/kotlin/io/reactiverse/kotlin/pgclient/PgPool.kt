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
import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.PgTransaction
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

suspend fun PgPool.preparedQueryAwait(sql: String): PgRowSet {
  return awaitResult {
    this.preparedQuery(sql, it)
  }
}

suspend fun PgPool.queryAwait(sql: String): PgRowSet {
  return awaitResult {
    this.query(sql, it)
  }
}

suspend fun PgPool.preparedQueryAwait(sql: String, arguments: Tuple): PgRowSet {
  return awaitResult {
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun PgPool.preparedBatchAwait(sql: String, batch: List<Tuple>): PgRowSet {
  return awaitResult {
    this.preparedBatch(sql, batch, it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgPool.getConnection]
 *
 * @return [PgConnection]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgPool] using Vert.x codegen.
 */
suspend fun PgPool.getConnectionAwait(): PgConnection {
  return awaitResult {
    this.getConnection(it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgPool.begin]
 *
 * @return [PgTransaction]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgPool] using Vert.x codegen.
 */
suspend fun PgPool.beginAwait(): PgTransaction {
  return awaitResult {
    this.begin(it)
  }
}


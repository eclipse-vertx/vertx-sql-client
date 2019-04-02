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

import io.reactiverse.pgclient.PgPreparedQuery
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.PgTransaction
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Suspending version of method [io.reactiverse.pgclient.PgTransaction.prepare]
 *
 * @param sql the sql
 * @return [PgPreparedQuery]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgTransaction] using Vert.x codegen.
 */
suspend fun PgTransaction.prepareAwait(sql: String): PgPreparedQuery {
  return awaitResult {
    this.prepare(sql, it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgTransaction.commit]
 *
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgTransaction] using Vert.x codegen.
 */
suspend fun PgTransaction.commitAwait(): Unit {
  return awaitResult {
    this.commit { ar -> it.handle(ar.mapEmpty()) }
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgTransaction.rollback]
 *
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgTransaction] using Vert.x codegen.
 */
suspend fun PgTransaction.rollbackAwait(): Unit {
  return awaitResult {
    this.rollback { ar -> it.handle(ar.mapEmpty()) }
  }
}

suspend fun PgTransaction.queryAwait(sql: String): PgRowSet {
  return awaitResult {
    this.query(sql, it)
  }
}

suspend fun PgTransaction.preparedQueryAwait(sql: String): PgRowSet {
  return awaitResult {
    this.preparedQuery(sql, it)
  }
}

suspend fun PgTransaction.preparedQueryAwait(sql: String, arguments: Tuple): PgRowSet {
  return awaitResult {
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun PgTransaction.preparedBatchAwait(sql: String, batch: List<Tuple>): PgRowSet {
  return awaitResult {
    this.preparedBatch(sql, batch, it)
  }
}


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
package io.vertx.kotlin.sqlclient

import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.sqlclient.PreparedQuery
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Transaction
import io.vertx.sqlclient.Tuple

/**
 * Suspending version of method [io.vertx.sqlclient.Transaction.prepare]
 *
 * @param sql the sql
 * @return [PreparedQuery]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.Transaction] using Vert.x codegen.
 */
suspend fun Transaction.prepareAwait(sql: String): PreparedQuery {
  return awaitResult {
    this.prepare(sql, it)
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.Transaction.commit]
 *
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.Transaction] using Vert.x codegen.
 */
suspend fun Transaction.commitAwait(): Unit {
  return awaitResult {
    this.commit { ar -> it.handle(ar.mapEmpty()) }
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.Transaction.rollback]
 *
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.Transaction] using Vert.x codegen.
 */
suspend fun Transaction.rollbackAwait(): Unit {
  return awaitResult {
    this.rollback { ar -> it.handle(ar.mapEmpty()) }
  }
}

suspend fun Transaction.queryAwait(sql: String): RowSet {
  return awaitResult {
    this.query(sql, it)
  }
}

suspend fun Transaction.preparedQueryAwait(sql: String): RowSet {
  return awaitResult {
    this.preparedQuery(sql, it)
  }
}

suspend fun Transaction.preparedQueryAwait(sql: String, arguments: Tuple): RowSet {
  return awaitResult {
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun Transaction.preparedBatchAwait(sql: String, batch: List<Tuple>): RowSet {
  return awaitResult {
    this.preparedBatch(sql, batch, it)
  }
}


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
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

/**
 * Suspending version of method [io.vertx.sqlclient.SqlClient.query]
 *
 * @param sql the query SQL
 * @return [RowSet]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.SqlClient] using Vert.x codegen.
 */
suspend fun SqlClient.queryAwait(sql: String): RowSet {
  return awaitResult {
    this.query(sql, it)
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.SqlClient.preparedQuery]
 *
 * @param sql the prepared query SQL
 * @return [RowSet]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.SqlClient] using Vert.x codegen.
 */
suspend fun SqlClient.preparedQueryAwait(sql: String): RowSet {
  return awaitResult {
    this.preparedQuery(sql, it)
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.SqlClient.preparedQuery]
 *
 * @param sql the prepared query SQL
 * @param arguments the list of arguments
 * @return [RowSet]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.SqlClient] using Vert.x codegen.
 */
suspend fun SqlClient.preparedQueryAwait(sql: String, arguments: Tuple): RowSet {
  return awaitResult {
    this.preparedQuery(sql, arguments, it)
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.SqlClient.preparedBatch]
 *
 * @param sql the prepared query SQL
 * @param batch the batch of tuples
 * @return [RowSet]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.SqlClient] using Vert.x codegen.
 */
suspend fun SqlClient.preparedBatchAwait(sql: String, batch: List<Tuple>): RowSet {
  return awaitResult {
    this.preparedBatch(sql, batch, it)
  }
}


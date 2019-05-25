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
import io.vertx.sqlclient.Tuple

/**
 * Suspending version of method [io.vertx.sqlclient.PreparedQuery.execute]
 *
 * @return [RowSet]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.PreparedQuery] using Vert.x codegen.
 */
suspend fun PreparedQuery.executeAwait(): RowSet {
  return awaitResult {
    this.execute(it)
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.PreparedQuery.execute]
 *
 * @param args the list of arguments
 * @return [RowSet]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.PreparedQuery] using Vert.x codegen.
 */
suspend fun PreparedQuery.executeAwait(args: Tuple): RowSet {
  return awaitResult {
    this.execute(args, it)
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.PreparedQuery.batch]
 *
 * @param argsList the list of tuple for the batch
 * @return [RowSet]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.PreparedQuery] using Vert.x codegen.
 */
suspend fun PreparedQuery.batchAwait(argsList: List<Tuple>): RowSet {
  return awaitResult {
    this.batch(argsList, it)
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.PreparedQuery.close]
 *
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.PreparedQuery] using Vert.x codegen.
 */
suspend fun PreparedQuery.closeAwait(): Unit {
  return awaitResult {
    this.close { ar -> it.handle(ar.mapEmpty()) }
  }
}


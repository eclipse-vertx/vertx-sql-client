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
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Suspending version of method [io.reactiverse.pgclient.PgPreparedQuery.execute]
 *
 * @return [PgRowSet]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgPreparedQuery] using Vert.x codegen.
 */
suspend fun PgPreparedQuery.executeAwait(): PgRowSet {
  return awaitResult {
    this.execute(it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgPreparedQuery.execute]
 *
 * @param args the list of arguments
 * @return [PgRowSet]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgPreparedQuery] using Vert.x codegen.
 */
suspend fun PgPreparedQuery.executeAwait(args: Tuple): PgRowSet {
  return awaitResult {
    this.execute(args, it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgPreparedQuery.batch]
 *
 * @param argsList the list of tuple for the batch
 * @return [PgRowSet]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgPreparedQuery] using Vert.x codegen.
 */
suspend fun PgPreparedQuery.batchAwait(argsList: List<Tuple>): PgRowSet {
  return awaitResult {
    this.batch(argsList, it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgPreparedQuery.close]
 *
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgPreparedQuery] using Vert.x codegen.
 */
suspend fun PgPreparedQuery.closeAwait(): Unit {
  return awaitResult {
    this.close { ar -> it.handle(ar.mapEmpty()) }
  }
}


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

import io.reactiverse.pgclient.PgCursor
import io.reactiverse.pgclient.PgRowSet
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Suspending version of method [io.reactiverse.pgclient.PgCursor.read]
 *
 * @param count the amount of rows to read
 * @return [PgRowSet]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgCursor] using Vert.x codegen.
 */
suspend fun PgCursor.readAwait(count: Int): PgRowSet {
  return awaitResult {
    this.read(count, it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgCursor.close]
 *
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgCursor] using Vert.x codegen.
 */
suspend fun PgCursor.closeAwait(): Unit {
  return awaitResult {
    this.close { ar -> it.handle(ar.mapEmpty()) }
  }
}


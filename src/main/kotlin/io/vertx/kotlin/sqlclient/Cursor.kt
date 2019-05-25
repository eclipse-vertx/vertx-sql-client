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
import io.vertx.sqlclient.Cursor
import io.vertx.sqlclient.RowSet

/**
 * Suspending version of method [io.vertx.sqlclient.Cursor.read]
 *
 * @param count the amount of rows to read
 * @return [RowSet]
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.Cursor] using Vert.x codegen.
 */
suspend fun Cursor.readAwait(count: Int): RowSet {
  return awaitResult {
    this.read(count, it)
  }
}

/**
 * Suspending version of method [io.vertx.sqlclient.Cursor.close]
 *
 *
 * NOTE: This function has been automatically generated from [io.vertx.sqlclient.Cursor] using Vert.x codegen.
 */
suspend fun Cursor.closeAwait(): Unit {
  return awaitResult {
    this.close { ar -> it.handle(ar.mapEmpty()) }
  }
}


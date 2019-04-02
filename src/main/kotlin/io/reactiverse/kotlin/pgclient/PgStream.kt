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

import io.reactiverse.pgclient.PgStream
import io.vertx.core.streams.WriteStream
import io.vertx.kotlin.coroutines.awaitResult

suspend fun <T> PgStream<T>.pipeToAwait(dst: WriteStream<T>): Unit {
  return awaitResult {
    this.pipeTo(dst) { ar -> it.handle(ar.mapEmpty()) }
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgStream.close]
 *
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgStream] using Vert.x codegen.
 */
suspend fun <T> PgStream<T>.closeAwait(): Unit {
  return awaitResult {
    this.close { ar -> it.handle(ar.mapEmpty()) }
  }
}


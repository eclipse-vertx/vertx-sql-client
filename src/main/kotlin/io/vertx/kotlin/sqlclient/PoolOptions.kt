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

import io.vertx.sqlclient.PoolOptions

/**
 * A function providing a DSL for building [io.vertx.sqlclient.PoolOptions] objects.
 *
 * The options for configuring a connection pool.
 *
 * @param maxSize  Set the maximum pool size
 * @param maxWaitQueueSize  Set the maximum connection request allowed in the wait queue, any requests beyond the max size will result in an failure.  If the value is set to a negative number then the queue will be unbounded.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.PoolOptions original] using Vert.x codegen.
 */
fun poolOptionsOf(
  maxSize: Int? = null,
  maxWaitQueueSize: Int? = null): PoolOptions = io.vertx.sqlclient.PoolOptions().apply {

  if (maxSize != null) {
    this.setMaxSize(maxSize)
  }
  if (maxWaitQueueSize != null) {
    this.setMaxWaitQueueSize(maxWaitQueueSize)
  }
}

/**
 * A function providing a DSL for building [io.vertx.sqlclient.PoolOptions] objects.
 *
 * The options for configuring a connection pool.
 *
 * @param maxSize  Set the maximum pool size
 * @param maxWaitQueueSize  Set the maximum connection request allowed in the wait queue, any requests beyond the max size will result in an failure.  If the value is set to a negative number then the queue will be unbounded.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.PoolOptions original] using Vert.x codegen.
 */
@Deprecated(
  message = "This function will be removed in a future version",
  replaceWith = ReplaceWith("poolOptionsOf(maxSize, maxWaitQueueSize)")
)
fun PoolOptions(
  maxSize: Int? = null,
  maxWaitQueueSize: Int? = null): PoolOptions = io.vertx.sqlclient.PoolOptions().apply {

  if (maxSize != null) {
    this.setMaxSize(maxSize)
  }
  if (maxWaitQueueSize != null) {
    this.setMaxWaitQueueSize(maxWaitQueueSize)
  }
}


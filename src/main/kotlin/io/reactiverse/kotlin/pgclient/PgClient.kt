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

import io.reactiverse.pgclient.PgClient as PgClientVertxAlias
import io.reactiverse.pgclient.PgConnectOptions
import io.reactiverse.pgclient.PgConnection
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Suspending version of method [io.reactiverse.pgclient.PgClient.query]
 *
 * @param sql the query SQL
 * @return [PgRowSet]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgClient] using Vert.x codegen.
 */
suspend fun PgClientVertxAlias.queryAwait(sql: String): PgRowSet {
  return awaitResult {
    this.query(sql, it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgClient.preparedQuery]
 *
 * @param sql the prepared query SQL
 * @return [PgRowSet]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgClient] using Vert.x codegen.
 */
suspend fun PgClientVertxAlias.preparedQueryAwait(sql: String): PgRowSet {
  return awaitResult {
    this.preparedQuery(sql, it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgClient.preparedQuery]
 *
 * @param sql the prepared query SQL
 * @param arguments the list of arguments
 * @return [PgRowSet]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgClient] using Vert.x codegen.
 */
suspend fun PgClientVertxAlias.preparedQueryAwait(sql: String, arguments: Tuple): PgRowSet {
  return awaitResult {
    this.preparedQuery(sql, arguments, it)
  }
}

/**
 * Suspending version of method [io.reactiverse.pgclient.PgClient.preparedBatch]
 *
 * @param sql the prepared query SQL
 * @param batch the batch of tuples
 * @return [PgRowSet]
 *
 * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgClient] using Vert.x codegen.
 */
suspend fun PgClientVertxAlias.preparedBatchAwait(sql: String, batch: List<Tuple>): PgRowSet {
  return awaitResult {
    this.preparedBatch(sql, batch, it)
  }
}

object PgClient {
  /**
   * Suspending version of method [io.reactiverse.pgclient.PgClient.connect]
   *
   * @param vertx the vertx instance
   * @param options the connect options
   * @return [PgConnection]
   *
   * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgClient] using Vert.x codegen.
   */
  suspend fun connectAwait(vertx: Vertx, options: PgConnectOptions): PgConnection {
    return awaitResult {
      PgClientVertxAlias.connect(vertx, options, it)
    }
  }

  /**
   * Suspending version of method [io.reactiverse.pgclient.PgClient.connect]
   *
   * @param vertx 
   * @return [PgConnection]
   *
   * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgClient] using Vert.x codegen.
   */
  suspend fun connectAwait(vertx: Vertx): PgConnection {
    return awaitResult {
      PgClientVertxAlias.connect(vertx, it)
    }
  }

  /**
   * Suspending version of method [io.reactiverse.pgclient.PgClient.connect]
   *
   * @param vertx 
   * @param connectionUri 
   * @return [PgConnection]
   *
   * NOTE: This function has been automatically generated from [io.reactiverse.pgclient.PgClient] using Vert.x codegen.
   */
  suspend fun connectAwait(vertx: Vertx, connectionUri: String): PgConnection {
    return awaitResult {
      PgClientVertxAlias.connect(vertx, connectionUri, it)
    }
  }

}

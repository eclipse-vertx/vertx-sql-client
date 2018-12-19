package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgConnection
import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.PgTransaction
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

suspend fun PgPool.preparedQueryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun PgPool.queryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun PgPool.preparedQueryAwait(sql : String, arguments : Tuple) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun PgPool.preparedBatchAwait(sql : String, batch : List<Tuple>) : PgRowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}

/**
 * Get a connection from the pool.
 *
 * @param handler the handler that will get the connection result
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgPool original] using Vert.x codegen.
 */
suspend fun PgPool.getConnectionAwait() : PgConnection {
  return awaitResult{
    this.getConnection(it)
  }
}

/**
 * Borrow a connection from the pool and begin a transaction, the underlying connection will be returned
 * to the pool when the transaction ends.
 *
 * @param handler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgPool original] using Vert.x codegen.
 */
suspend fun PgPool.beginAwait() : PgTransaction {
  return awaitResult{
    this.begin(it)
  }
}


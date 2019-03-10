package io.reactiverse.kotlin.sqlclient

import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import io.reactiverse.sqlclient.SqlConnection
import io.reactiverse.sqlclient.SqlPool
import io.reactiverse.sqlclient.SqlTransaction
import io.vertx.kotlin.coroutines.awaitResult

suspend fun SqlPool.preparedQueryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun SqlPool.queryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun SqlPool.preparedQueryAwait(sql : String, arguments : Tuple) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun SqlPool.preparedBatchAwait(sql : String, batch : List<Tuple>) : PgRowSet {
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
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.SqlPool original] using Vert.x codegen.
 */
suspend fun SqlPool.getConnectionAwait() : SqlConnection {
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
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.SqlPool original] using Vert.x codegen.
 */
suspend fun SqlPool.beginAwait() : SqlTransaction {
  return awaitResult{
    this.begin(it)
  }
}


package io.reactiverse.kotlin.sqlclient

import io.reactiverse.sqlclient.Pool
import io.reactiverse.sqlclient.RowSet
import io.reactiverse.sqlclient.SqlConnection
import io.reactiverse.sqlclient.Transaction
import io.reactiverse.sqlclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

suspend fun Pool.preparedQueryAwait(sql : String) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun Pool.queryAwait(sql : String) : RowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun Pool.preparedQueryAwait(sql : String, arguments : Tuple) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun Pool.preparedBatchAwait(sql : String, batch : List<Tuple>) : RowSet {
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
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.Pool original] using Vert.x codegen.
 */
suspend fun Pool.getConnectionAwait() : SqlConnection {
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
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.Pool original] using Vert.x codegen.
 */
suspend fun Pool.beginAwait() : Transaction {
  return awaitResult{
    this.begin(it)
  }
}


package io.vertx.kotlin.sqlclient

import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Transaction
import io.vertx.sqlclient.Tuple

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
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.Pool original] using Vert.x codegen.
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
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.Pool original] using Vert.x codegen.
 */
suspend fun Pool.beginAwait() : Transaction {
  return awaitResult{
    this.begin(it)
  }
}


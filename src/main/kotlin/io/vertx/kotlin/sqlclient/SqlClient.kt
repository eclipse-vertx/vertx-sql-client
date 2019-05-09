package io.vertx.kotlin.sqlclient

import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

/**
 * Execute a simple query.
 *
 * @param sql the query SQL
 * @param handler the handler notified with the execution result
 * @returna reference to this, so the API can be used fluently *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.SqlClient original] using Vert.x codegen.
 */
suspend fun SqlClient.queryAwait(sql : String) : RowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

/**
 * Prepare and execute a query.
 *
 * @param sql the prepared query SQL
 * @param handler the handler notified with the execution result
 * @returna reference to this, so the API can be used fluently *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.SqlClient original] using Vert.x codegen.
 */
suspend fun SqlClient.preparedQueryAwait(sql : String) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

/**
 * Prepare and execute a query.
 *
 * @param sql the prepared query SQL
 * @param arguments the list of arguments
 * @param handler the handler notified with the execution result
 * @returna reference to this, so the API can be used fluently *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.SqlClient original] using Vert.x codegen.
 */
suspend fun SqlClient.preparedQueryAwait(sql : String, arguments : Tuple) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

/**
 * Prepare and execute a createBatch.
 *
 * @param sql the prepared query SQL
 * @param batch the batch of tuples
 * @param handler the handler notified with the execution result
 * @returna reference to this, so the API can be used fluently *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.SqlClient original] using Vert.x codegen.
 */
suspend fun SqlClient.preparedBatchAwait(sql : String, batch : List<Tuple>) : RowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}


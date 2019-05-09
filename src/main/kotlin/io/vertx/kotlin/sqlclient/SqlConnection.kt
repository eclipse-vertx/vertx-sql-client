package io.vertx.kotlin.sqlclient

import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.sqlclient.PreparedQuery
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

/**
 * Create a prepared query.
 *
 * @param sql the sql
 * @param handler the handler notified with the prepared query asynchronously
 * @return *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.SqlConnection original] using Vert.x codegen.
 */
suspend fun SqlConnection.prepareAwait(sql : String) : PreparedQuery {
  return awaitResult{
    this.prepare(sql, it)
  }
}

suspend fun SqlConnection.preparedQueryAwait(sql : String) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun SqlConnection.queryAwait(sql : String) : RowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun SqlConnection.preparedQueryAwait(sql : String, arguments : Tuple) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun SqlConnection.preparedBatchAwait(sql : String, batch : List<Tuple>) : RowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}


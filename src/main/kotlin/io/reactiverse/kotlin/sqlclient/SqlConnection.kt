package io.reactiverse.kotlin.sqlclient

import io.reactiverse.sqlclient.PreparedQuery
import io.reactiverse.sqlclient.RowSet
import io.reactiverse.sqlclient.SqlConnection
import io.reactiverse.sqlclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Create a prepared query.
 *
 * @param sql the sql
 * @param handler the handler notified with the prepared query asynchronously
 * @return *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.SqlConnection original] using Vert.x codegen.
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


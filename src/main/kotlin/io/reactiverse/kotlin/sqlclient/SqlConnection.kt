package io.reactiverse.kotlin.sqlclient

import io.reactiverse.pgclient.PgPreparedQuery
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import io.reactiverse.sqlclient.SqlConnection
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
suspend fun SqlConnection.prepareAwait(sql : String) : PgPreparedQuery {
  return awaitResult{
    this.prepare(sql, it)
  }
}

suspend fun SqlConnection.preparedQueryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun SqlConnection.queryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun SqlConnection.preparedQueryAwait(sql : String, arguments : Tuple) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun SqlConnection.preparedBatchAwait(sql : String, batch : List<Tuple>) : PgRowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}


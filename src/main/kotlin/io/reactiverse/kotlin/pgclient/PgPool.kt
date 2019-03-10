package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgPool
import io.reactiverse.sqlclient.RowSet
import io.reactiverse.sqlclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

suspend fun PgPool.preparedQueryAwait(sql : String) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun PgPool.queryAwait(sql : String) : RowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun PgPool.preparedQueryAwait(sql : String, arguments : Tuple) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun PgPool.preparedBatchAwait(sql : String, batch : List<Tuple>) : RowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}


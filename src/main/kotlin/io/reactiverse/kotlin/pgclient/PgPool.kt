package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgRowSet
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


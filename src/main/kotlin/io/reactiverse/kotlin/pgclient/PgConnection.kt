package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgConnection
import io.reactiverse.pgclient.PgPreparedQuery
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

suspend fun PgConnection.prepareAwait(sql : String) : PgPreparedQuery {
  return awaitResult{
    this.prepare(sql, it)
  }
}

suspend fun PgConnection.preparedQueryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun PgConnection.queryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun PgConnection.preparedQueryAwait(sql : String, arguments : Tuple) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun PgConnection.preparedBatchAwait(sql : String, batch : List<Tuple>) : PgRowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}


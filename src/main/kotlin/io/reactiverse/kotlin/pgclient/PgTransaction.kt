package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgPreparedQuery
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.PgTransaction
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Create a prepared query.
 *
 * @param sql the sql
 * @param handler the handler notified with the prepared query asynchronously
 * @return *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgTransaction original] using Vert.x codegen.
 */
suspend fun PgTransaction.prepareAwait(sql : String) : PgPreparedQuery {
  return awaitResult{
    this.prepare(sql, it)
  }
}

/**
 * Like [io.reactiverse.pgclient.PgTransaction] with an handler to be notified when the transaction commit has completed
 *
 * @param handler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgTransaction original] using Vert.x codegen.
 */
suspend fun PgTransaction.commitAwait() : Unit {
  return awaitResult{
    this.commit({ ar -> it.handle(ar.mapEmpty()) })}
}

/**
 * Like [io.reactiverse.pgclient.PgTransaction] with an handler to be notified when the transaction rollback has completed
 *
 * @param handler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgTransaction original] using Vert.x codegen.
 */
suspend fun PgTransaction.rollbackAwait() : Unit {
  return awaitResult{
    this.rollback({ ar -> it.handle(ar.mapEmpty()) })}
}

suspend fun PgTransaction.queryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun PgTransaction.preparedQueryAwait(sql : String) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun PgTransaction.preparedQueryAwait(sql : String, arguments : Tuple) : PgRowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun PgTransaction.preparedBatchAwait(sql : String, batch : List<Tuple>) : PgRowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}


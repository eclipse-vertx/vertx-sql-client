package io.reactiverse.kotlin.sqlclient

import io.reactiverse.sqlclient.PreparedQuery
import io.reactiverse.sqlclient.RowSet
import io.reactiverse.sqlclient.Transaction
import io.reactiverse.sqlclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Create a prepared query.
 *
 * @param sql the sql
 * @param handler the handler notified with the prepared query asynchronously
 * @return *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.Transaction original] using Vert.x codegen.
 */
suspend fun Transaction.prepareAwait(sql : String) : PreparedQuery {
  return awaitResult{
    this.prepare(sql, it)
  }
}

/**
 * Like [io.reactiverse.sqlclient.Transaction] with an handler to be notified when the transaction commit has completed
 *
 * @param handler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.Transaction original] using Vert.x codegen.
 */
suspend fun Transaction.commitAwait() : Unit {
  return awaitResult{
    this.commit({ ar -> it.handle(ar.mapEmpty()) })}
}

/**
 * Like [io.reactiverse.sqlclient.Transaction] with an handler to be notified when the transaction rollback has completed
 *
 * @param handler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.Transaction original] using Vert.x codegen.
 */
suspend fun Transaction.rollbackAwait() : Unit {
  return awaitResult{
    this.rollback({ ar -> it.handle(ar.mapEmpty()) })}
}

suspend fun Transaction.queryAwait(sql : String) : RowSet {
  return awaitResult{
    this.query(sql, it)
  }
}

suspend fun Transaction.preparedQueryAwait(sql : String) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, it)
  }
}

suspend fun Transaction.preparedQueryAwait(sql : String, arguments : Tuple) : RowSet {
  return awaitResult{
    this.preparedQuery(sql, arguments, it)
  }
}

suspend fun Transaction.preparedBatchAwait(sql : String, batch : List<Tuple>) : RowSet {
  return awaitResult{
    this.preparedBatch(sql, batch, it)
  }
}


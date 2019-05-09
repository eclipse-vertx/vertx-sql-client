package io.vertx.kotlin.sqlclient

import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.sqlclient.PreparedQuery
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

/**
 * Calls [io.vertx.sqlclient.PreparedQuery] with an empty tuple argument.
 *
 * @param handler 
 * @return *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.PreparedQuery original] using Vert.x codegen.
 */
suspend fun PreparedQuery.executeAwait() : RowSet {
  return awaitResult{
    this.execute(it)
  }
}

/**
 * Create a cursor with the provided <code>arguments</code>.
 *
 * @param args the list of arguments
 * @param handler 
 * @returnthe query *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.PreparedQuery original] using Vert.x codegen.
 */
suspend fun PreparedQuery.executeAwait(args : Tuple) : RowSet {
  return awaitResult{
    this.execute(args, it)
  }
}

/**
 * Execute a batch.
 *
 * @param argsList the list of tuple for the batch
 * @param handler 
 * @returnthe createBatch *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.PreparedQuery original] using Vert.x codegen.
 */
suspend fun PreparedQuery.batchAwait(argsList : List<Tuple>) : RowSet {
  return awaitResult{
    this.batch(argsList, it)
  }
}

/**
 * Like [io.vertx.sqlclient.PreparedQuery] but notifies the <code>completionHandler</code> when it's closed.
 *
 * @param completionHandler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.PreparedQuery original] using Vert.x codegen.
 */
suspend fun PreparedQuery.closeAwait() : Unit {
  return awaitResult{
    this.close({ ar -> it.handle(ar.mapEmpty()) })}
}


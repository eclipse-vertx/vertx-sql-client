package io.reactiverse.kotlin.sqlclient

import io.reactiverse.sqlclient.PreparedQuery
import io.reactiverse.sqlclient.RowSet
import io.reactiverse.sqlclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Calls [io.reactiverse.sqlclient.PreparedQuery] with an empty tuple argument.
 *
 * @param handler 
 * @return *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.PreparedQuery original] using Vert.x codegen.
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
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.PreparedQuery original] using Vert.x codegen.
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
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.PreparedQuery original] using Vert.x codegen.
 */
suspend fun PreparedQuery.batchAwait(argsList : List<Tuple>) : RowSet {
  return awaitResult{
    this.batch(argsList, it)
  }
}

/**
 * Like [io.reactiverse.sqlclient.PreparedQuery] but notifies the <code>completionHandler</code> when it's closed.
 *
 * @param completionHandler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.PreparedQuery original] using Vert.x codegen.
 */
suspend fun PreparedQuery.closeAwait() : Unit {
  return awaitResult{
    this.close({ ar -> it.handle(ar.mapEmpty()) })}
}


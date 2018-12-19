package io.reactiverse.kotlin.pgclient

import io.reactiverse.pgclient.PgPreparedQuery
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Calls [io.reactiverse.pgclient.PgPreparedQuery] with an empty tuple argument.
 *
 * @param handler 
 * @return *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgPreparedQuery original] using Vert.x codegen.
 */
suspend fun PgPreparedQuery.executeAwait() : PgRowSet {
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
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgPreparedQuery original] using Vert.x codegen.
 */
suspend fun PgPreparedQuery.executeAwait(args : Tuple) : PgRowSet {
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
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgPreparedQuery original] using Vert.x codegen.
 */
suspend fun PgPreparedQuery.batchAwait(argsList : List<Tuple>) : PgRowSet {
  return awaitResult{
    this.batch(argsList, it)
  }
}

/**
 * Like [io.reactiverse.pgclient.PgPreparedQuery] but notifies the <code>completionHandler</code> when it's closed.
 *
 * @param completionHandler 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.pgclient.PgPreparedQuery original] using Vert.x codegen.
 */
suspend fun PgPreparedQuery.closeAwait() : Unit {
  return awaitResult{
    this.close({ ar -> it.handle(ar.mapEmpty()) })}
}


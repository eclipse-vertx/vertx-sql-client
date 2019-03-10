package io.reactiverse.kotlin.sqlclient

import io.reactiverse.sqlclient.RowStream
import io.vertx.kotlin.coroutines.awaitResult

/**
 * Close the stream and release the resources.
 *
 * @param completionHandler the completion handler for this operation
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.reactiverse.sqlclient.RowStream original] using Vert.x codegen.
 */
suspend fun <T> RowStream<T>.closeAwait() : Unit {
  return awaitResult{
    this.close({ ar -> it.handle(ar.mapEmpty()) })}
}


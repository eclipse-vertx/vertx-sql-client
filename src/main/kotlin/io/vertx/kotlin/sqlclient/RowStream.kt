package io.vertx.kotlin.sqlclient

import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.sqlclient.RowStream

/**
 * Close the stream and release the resources.
 *
 * @param completionHandler the completion handler for this operation
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.sqlclient.RowStream original] using Vert.x codegen.
 */
suspend fun <T> RowStream<T>.closeAwait() : Unit {
  return awaitResult{
    this.close({ ar -> it.handle(ar.mapEmpty()) })}
}

